package com.ecclesiaflow.communication.application.messaging;

import com.ecclesiaflow.communication.business.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ listener for processing queued emails.
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
     * @param message Email message to process
     */
    @RabbitListener(
        queues = "ecclesiaflow.email.send.queue",
        concurrency = "3-10"
    )
    public void handleEmailMessage(EmailMessage message) {
        // Processing is automatically logged by BusinessServiceLoggingAspect
        emailService.findById(message.getEmailId())
                .ifPresent(emailService::sendEmail);
    }
    
    /**
     * Listens and processes failed communication retries.
     *
     * @param message Email message to retry
     */
    @RabbitListener(
        queues = "ecclesiaflow.email.retry.queue",
        concurrency = "2-5"
    )
    public void handleRetryMessage(EmailMessage message) {
        emailService.findById(message.getEmailId())
                .ifPresent(email -> {
                    if (email.getRetryCount() < 3) {
                        emailService.sendEmail(email.incrementRetryCount());
                    }
                });
    }
}
