package com.ecclesiaflow.communication.application.messaging;

import com.ecclesiaflow.communication.business.domain.email.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ publisher for communication queue publishing.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "rabbitmq.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class EmailMessagePublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange.email:ecclesiaflow.communication.exchange}")
    private String emailExchange;
    
    public void publishEmailForSending(Email email) {
        publish(email, "communication.send");
    }
    
    public void publishEmailForRetry(Email email) {
        publish(email, "communication.retry");
    }
    
    private void publish(Email email, String routingKeyPrefix) {
        EmailMessage message = buildMessage(email);
        String routingKey = routingKeyPrefix + "." + email.getPriority().name().toLowerCase();
        rabbitTemplate.convertAndSend(emailExchange, routingKey, message);
    }
    
    private EmailMessage buildMessage(Email email) {
        return EmailMessage.builder()
                .emailId(email.getId())
                .from(email.getFromAddress())
                .to(email.getToAddresses())
                .subject(email.getSubject())
                .templateName(email.getTemplateName())
                .variables(email.getVariables())
                .priority(email.getPriority().name())
                .retryCount(email.getRetryCount())
                .build();
    }
}
