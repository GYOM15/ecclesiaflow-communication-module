package com.ecclesiaflow.communication.application.messaging;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.grpc.email.EmailQueueMessage;
import com.ecclesiaflow.grpc.email.Priority;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * RabbitMQ publisher for communication queue publishing.
 * Uses Protobuf EmailQueueMessage for efficient serialization.
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
        EmailQueueMessage message = buildMessage(email);
        String routingKey = routingKeyPrefix + "." + email.getPriority().name().toLowerCase();
        rabbitTemplate.convertAndSend(emailExchange, routingKey, message);
    }
    
    private EmailQueueMessage buildMessage(Email email) {
        EmailQueueMessage.Builder builder = EmailQueueMessage.newBuilder()
                .setEmailId(email.getId().toString())
                .addAllTo(email.getToAddresses())
                .setSubject(email.getSubject())
                .setTemplateName(email.getTemplateName())
                .putAllVariables(convertVariables(email.getVariables()))
                .setPriority(mapPriority(email.getPriority()))
                .setRetryCount(email.getRetryCount());
        
        if (email.getFromAddress() != null) {
            builder.setFrom(email.getFromAddress());
        }
        
        return builder.build();
    }
    
    private Map<String, String> convertVariables(Map<String, Object> variables) {
        if (variables == null) {
            return Map.of();
        }
        return variables.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() != null ? e.getValue().toString() : ""
                ));
    }
    
    private Priority mapPriority(com.ecclesiaflow.communication.business.domain.email.EmailPriority priority) {
        return switch (priority) {
            case HIGH -> Priority.PRIORITY_HIGH;
            case NORMAL -> Priority.PRIORITY_NORMAL;
            case LOW -> Priority.PRIORITY_LOW;
        };
    }
}
