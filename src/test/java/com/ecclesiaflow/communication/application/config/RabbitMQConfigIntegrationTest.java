package com.ecclesiaflow.communication.application.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RabbitMQConfigIntegrationTest {

    private RabbitMQConfig config;

    @BeforeEach
    void setUp() throws Exception {
        config = new RabbitMQConfig();
        setField("emailExchange", "ecclesiaflow.communication.exchange");
        setField("emailQueue", "ecclesiaflow.communication.send.queue");
        setField("emailRetryQueue", "ecclesiaflow.communication.retry.queue");
        setField("emailDlq", "ecclesiaflow.communication.dlq");
        setField("emailRoutingKey", "communication.send.*");
        setField("emailRetryRoutingKey", "communication.retry.*");
    }

    private void setField(String fieldName, String value) throws Exception {
        Field field = RabbitMQConfig.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(config, value);
    }

    @Test
    void emailExchange_shouldBeDurable() {
        TopicExchange exchange = config.emailExchange();

        assertThat(exchange).isNotNull();
        assertThat(exchange.getName()).isEqualTo("ecclesiaflow.communication.exchange");
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
    }

    @Test
    void emailQueue_shouldBeDurableWithDLQArguments() {
        Queue queue = config.emailQueue();

        assertThat(queue).isNotNull();
        assertThat(queue.getName()).isEqualTo("ecclesiaflow.communication.send.queue");
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments()).containsKey("x-dead-letter-exchange");
        assertThat(queue.getArguments()).containsKey("x-dead-letter-routing-key");
    }

    @Test
    void emailRetryQueue_shouldHaveTTLAndDLQArguments() {
        Queue queue = config.emailRetryQueue();

        assertThat(queue).isNotNull();
        assertThat(queue.getName()).isEqualTo("ecclesiaflow.communication.retry.queue");
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments()).containsKey("x-message-ttl");
        assertThat(queue.getArguments().get("x-message-ttl")).isEqualTo(300000);
    }

    @Test
    void emailDeadLetterQueue_shouldBeDurable() {
        Queue queue = config.emailDeadLetterQueue();

        assertThat(queue).isNotNull();
        assertThat(queue.getName()).isEqualTo("ecclesiaflow.communication.dlq");
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    void emailQueueBinding_shouldBindToExchange() {
        Queue queue = config.emailQueue();
        TopicExchange exchange = config.emailExchange();

        Binding binding = config.emailQueueBinding(queue, exchange);

        assertThat(binding).isNotNull();
        assertThat(binding.getDestination()).isEqualTo(queue.getName());
        assertThat(binding.getExchange()).isEqualTo(exchange.getName());
        assertThat(binding.getRoutingKey()).isEqualTo("communication.send.*");
    }

    @Test
    void emailRetryQueueBinding_shouldBindToExchange() {
        Queue queue = config.emailRetryQueue();
        TopicExchange exchange = config.emailExchange();

        Binding binding = config.emailRetryQueueBinding(queue, exchange);

        assertThat(binding).isNotNull();
        assertThat(binding.getDestination()).isEqualTo(queue.getName());
        assertThat(binding.getExchange()).isEqualTo(exchange.getName());
        assertThat(binding.getRoutingKey()).isEqualTo("communication.retry.*");
    }

    @Test
    void emailDlqBinding_shouldBindToExchange() {
        Queue queue = config.emailDeadLetterQueue();
        TopicExchange exchange = config.emailExchange();

        Binding binding = config.emailDlqBinding(queue, exchange);

        assertThat(binding).isNotNull();
        assertThat(binding.getDestination()).isEqualTo(queue.getName());
        assertThat(binding.getExchange()).isEqualTo(exchange.getName());
        assertThat(binding.getRoutingKey()).isEqualTo("communication.dead");
    }

    @Test
    void protobufMessageConverter_shouldBeProtobufConverter() {
        MessageConverter converter = config.protobufMessageConverter();

        assertThat(converter).isNotNull();
        assertThat(converter).isInstanceOf(ProtobufMessageConverter.class);
    }

    @Test
    void rabbitTemplate_shouldUseProtobufConverter() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        MessageConverter converter = config.protobufMessageConverter();

        RabbitTemplate template = config.rabbitTemplate(connectionFactory, converter);

        assertThat(template).isNotNull();
        assertThat(template.getMessageConverter()).isInstanceOf(ProtobufMessageConverter.class);
    }
}
