package com.ecclesiaflow.communication.application.messaging;

import com.ecclesiaflow.communication.business.services.EmailService;
import com.ecclesiaflow.grpc.email.EmailQueueMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * RabbitMQ listener for processing queued emails.
 * Uses Protobuf EmailQueueMessage for efficient deserialization.
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
public class EmailMessageListener {
    
    private final EmailService emailService;
    
    /**
     * Listens and processes communication sending messages.
     *
     * @param message Protobuf email message to process
     */
    @RabbitListener(
        queues = "ecclesiaflow.email.send.queue",
        concurrency = "3-10"
    )
    public void handleEmailMessage(EmailQueueMessage message) {
        UUID emailId = UUID.fromString(message.getEmailId());
        emailService.findById(emailId)
                .ifPresent(emailService::sendEmail);
    }
    
    /**
     * Listens and processes failed communication retries.
     *
     * @param message Protobuf email message to retry
     */
    @RabbitListener(
        queues = "ecclesiaflow.email.retry.queue",
        concurrency = "2-5"
    )
    public void handleRetryMessage(EmailQueueMessage message) {
        UUID emailId = UUID.fromString(message.getEmailId());
        emailService.findById(emailId)
                .ifPresent(email -> {
                    if (email.getRetryCount() < 3) {
                        emailService.sendEmail(email.incrementRetryCount());
                    }
                });
    }
}
