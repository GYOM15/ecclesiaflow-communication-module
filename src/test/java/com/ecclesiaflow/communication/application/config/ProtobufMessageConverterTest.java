package com.ecclesiaflow.communication.application.config;

import com.ecclesiaflow.grpc.email.EmailQueueMessage;
import com.ecclesiaflow.grpc.email.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProtobufMessageConverterTest {

    private ProtobufMessageConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ProtobufMessageConverter(EmailQueueMessage.class);
    }

    @Test
    void toMessage_shouldSerializeProtobufMessage() {
        EmailQueueMessage protoMessage = EmailQueueMessage.newBuilder()
                .setEmailId("test-id")
                .setSubject("Test Subject")
                .setPriority(Priority.PRIORITY_HIGH)
                .build();

        Message message = converter.toMessage(protoMessage, new MessageProperties());

        assertThat(message).isNotNull();
        assertThat(message.getMessageProperties().getContentType()).isEqualTo("application/x-protobuf");
        assertThat((String) message.getMessageProperties().getHeader("__TypeId__"))
                .isEqualTo(EmailQueueMessage.class.getName());
        assertThat(message.getBody().length).isGreaterThan(0);
    }

    @Test
    void toMessage_shouldThrowExceptionForNonProtobufObject() {
        String nonProtoObject = "not a protobuf";

        assertThatThrownBy(() -> converter.toMessage(nonProtoObject, new MessageProperties()))
                .isInstanceOf(MessageConversionException.class)
                .hasMessageContaining("Object must be a Protobuf Message");
    }

    @Test
    void fromMessage_shouldDeserializeProtobufMessage() {
        EmailQueueMessage original = EmailQueueMessage.newBuilder()
                .setEmailId("test-id")
                .setSubject("Test Subject")
                .addTo("recipient@test.com")
                .setPriority(Priority.PRIORITY_NORMAL)
                .setRetryCount(2)
                .build();

        MessageProperties props = new MessageProperties();
        props.setHeader("__TypeId__", EmailQueueMessage.class.getName());
        Message message = new Message(original.toByteArray(), props);

        Object result = converter.fromMessage(message);

        assertThat(result).isInstanceOf(EmailQueueMessage.class);
        EmailQueueMessage deserialized = (EmailQueueMessage) result;
        assertThat(deserialized.getEmailId()).isEqualTo("test-id");
        assertThat(deserialized.getSubject()).isEqualTo("Test Subject");
        assertThat(deserialized.getToList()).containsExactly("recipient@test.com");
        assertThat(deserialized.getPriority()).isEqualTo(Priority.PRIORITY_NORMAL);
        assertThat(deserialized.getRetryCount()).isEqualTo(2);
    }

    @Test
    void fromMessage_shouldUseDefaultTypeWhenTypeIdMissing() {
        EmailQueueMessage original = EmailQueueMessage.newBuilder()
                .setEmailId("default-type-test")
                .build();

        MessageProperties props = new MessageProperties();
        Message message = new Message(original.toByteArray(), props);

        Object result = converter.fromMessage(message);

        assertThat(result).isInstanceOf(EmailQueueMessage.class);
        EmailQueueMessage deserialized = (EmailQueueMessage) result;
        assertThat(deserialized.getEmailId()).isEqualTo("default-type-test");
    }

    @Test
    void fromMessage_shouldUseDefaultTypeWhenTypeIdClassNotFound() {
        EmailQueueMessage original = EmailQueueMessage.newBuilder()
                .setEmailId("fallback-test")
                .build();

        MessageProperties props = new MessageProperties();
        props.setHeader("__TypeId__", "com.ecclesiaflow.nonexistent.Class");
        Message message = new Message(original.toByteArray(), props);

        Object result = converter.fromMessage(message);

        assertThat(result).isInstanceOf(EmailQueueMessage.class);
    }

    @Test
    void roundTrip_shouldPreserveAllFields() {
        EmailQueueMessage original = EmailQueueMessage.newBuilder()
                .setEmailId("round-trip-id")
                .setFrom("sender@test.com")
                .addTo("recipient1@test.com")
                .addTo("recipient2@test.com")
                .setSubject("Round Trip Test")
                .setTemplateName("emails/test-template")
                .putVariables("key1", "value1")
                .putVariables("key2", "value2")
                .setPriority(Priority.PRIORITY_LOW)
                .setRetryCount(5)
                .build();

        Message amqpMessage = converter.toMessage(original, new MessageProperties());
        Object result = converter.fromMessage(amqpMessage);

        assertThat(result).isInstanceOf(EmailQueueMessage.class);
        EmailQueueMessage deserialized = (EmailQueueMessage) result;
        assertThat(deserialized).isEqualTo(original);
    }
}
