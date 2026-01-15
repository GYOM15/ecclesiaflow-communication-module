package com.ecclesiaflow.communication.application.messaging;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.email.EmailPriority;
import com.ecclesiaflow.grpc.email.EmailQueueMessage;
import com.ecclesiaflow.grpc.email.Priority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailMessagePublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private EmailMessagePublisher newPublisherWithExchange(String exchange) throws Exception {
        EmailMessagePublisher publisher = new EmailMessagePublisher(rabbitTemplate);
        Field f = EmailMessagePublisher.class.getDeclaredField("emailExchange");
        f.setAccessible(true);
        f.set(publisher, exchange);
        return publisher;
    }

    private Email sampleEmail(EmailPriority priority) {
        return Email.builder()
                .id(UUID.randomUUID())
                .fromAddress("from@ecclesiaflow.com")
                .toAddresses(List.of("to@example.com"))
                .subject("Subject")
                .templateName("emails/test")
                .variables(Map.of("k", "v"))
                .priority(priority)
                .retryCount(1)
                .build();
    }

    @Test
    void publishEmailForSending_shouldSendToExchangeWithRoutingKey() throws Exception {
        EmailMessagePublisher publisher = newPublisherWithExchange("ex");
        Email email = sampleEmail(EmailPriority.NORMAL);

        ArgumentCaptor<String> routingKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EmailQueueMessage> messageCaptor = ArgumentCaptor.forClass(EmailQueueMessage.class);

        publisher.publishEmailForSending(email);

        verify(rabbitTemplate).convertAndSend(eq("ex"), routingKey.capture(), messageCaptor.capture());
        assertThat(routingKey.getValue()).isEqualTo("communication.send.normal");
        EmailQueueMessage msg = messageCaptor.getValue();
        assertThat(msg.getEmailId()).isEqualTo(email.getId().toString());
        assertThat(msg.getFrom()).isEqualTo(email.getFromAddress());
        assertThat(msg.getToList()).containsExactlyElementsOf(email.getToAddresses());
        assertThat(msg.getTemplateName()).isEqualTo(email.getTemplateName());
        assertThat(msg.getPriority()).isEqualTo(Priority.PRIORITY_NORMAL);
        assertThat(msg.getRetryCount()).isEqualTo(email.getRetryCount());
    }

    @Test
    void publishEmailForRetry_shouldSendToExchangeWithRoutingKey() throws Exception {
        EmailMessagePublisher publisher = newPublisherWithExchange("ex");
        Email email = sampleEmail(EmailPriority.HIGH);

        ArgumentCaptor<String> routingKey = ArgumentCaptor.forClass(String.class);

        publisher.publishEmailForRetry(email);

        verify(rabbitTemplate).convertAndSend(eq("ex"), routingKey.capture(), org.mockito.Mockito.any(EmailQueueMessage.class));
        assertThat(routingKey.getValue()).isEqualTo("communication.retry.high");
    }
}
