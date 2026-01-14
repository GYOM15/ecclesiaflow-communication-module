package com.ecclesiaflow.communication.application.jobs;

import com.ecclesiaflow.communication.business.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job for processing queued emails and retrying failed ones.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "communication.queue.processor.enabled", havingValue = "true", matchIfMissing = true)
public class EmailQueueProcessor {
    
    private final EmailService emailService;
    
    @Scheduled(fixedDelay = 5000, initialDelay = 2000)
    public void processQueuedEmails() {
        emailService.processQueuedEmails(10);
    }
    
    @Scheduled(fixedDelay = 120000, initialDelay = 30000)
    public void retryFailedEmails() {
        emailService.retryFailedEmails(3, 5);
    }
}
