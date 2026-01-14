package com.ecclesiaflow.communication.business.services.impl;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.email.EmailRepository;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import com.ecclesiaflow.communication.business.domain.provider.EmailProvider;
import com.ecclesiaflow.communication.business.domain.provider.SendResult;
import com.ecclesiaflow.communication.business.exceptions.EmailSendingException;
import com.ecclesiaflow.communication.business.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Email service implementation with provider failover and retry support.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailRepository emailRepository;
    private final List<EmailProvider> emailProviders;

    @Override
    @Transactional
    public Email sendEmail(Email email) {
        Email emailToSend = email.getId() == null
                ? emailRepository.save(email.toBuilder()
                .status(EmailStatus.QUEUED)
                .queuedAt(LocalDateTime.now())
                .build())
                : email;

        // Select available provider by priority; fallback to queue if none available
        EmailProvider provider = selectAvailableProvider();

        if (provider == null) {
            // No provider available: ensure communication is queued and persisted
            if (email.getId() != null) {
                Email queued = email.toBuilder()
                        .status(EmailStatus.QUEUED)
                        .queuedAt(LocalDateTime.now())
                        .build();
                return emailRepository.save(queued);
            }
            return emailToSend; // already queued above
        }

        try {
            SendResult result = provider.send(emailToSend);
            Email updatedEmail = result.isSuccess()
                    ? emailToSend.markAsSent(result.getProviderName(), result.getMessageId())
                    : emailToSend.markAsFailed(result.getErrorMessage());

            return emailRepository.save(updatedEmail);

        } catch (EmailSendingException e) {
            Email failedEmail = emailToSend.markAsFailed(e.getMessage());
            return emailRepository.save(failedEmail);
        }
    }

    @Override
    @Transactional
    public Email queueEmail(Email email) {
        return emailRepository.save(email.toBuilder()
                .status(EmailStatus.QUEUED)
                .queuedAt(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Email> findById(UUID emailId) {
        return emailRepository.findById(emailId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Email> findAll(Pageable pageable) {
        return emailRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Email> findByStatus(EmailStatus status, Pageable pageable) {
        return emailRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional
    public int processQueuedEmails(int batchSize) {
        return processEmails(emailRepository.findQueuedEmails(batchSize));
    }

    @Override
    @Transactional
    public int retryFailedEmails(int maxRetryCount, int batchSize) {
        List<Email> failedEmails = emailRepository.findFailedEmailsForRetry(maxRetryCount, batchSize);
        return processEmails(failedEmails.stream()
                .map(Email::incrementRetryCount)
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(EmailStatus status) {
        return emailRepository.countByStatus(status);
    }

    private int processEmails(List<Email> emails) {
        int successCount = 0;
        for (Email email : emails) {
            try {
                sendEmail(email);
                successCount++;
            } catch (Exception e) {
                // Logged by aspect
            }
        }
        return successCount;
    }

    private EmailProvider selectAvailableProvider() {
        return emailProviders.stream()
                .sorted(Comparator.comparingInt(EmailProvider::getPriority))
                .filter(EmailProvider::isAvailable)
                .findFirst()
                .orElse(null);
    }
}
