package com.ecclesiaflow.communication.business.services;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Email management service interface.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public interface EmailService {

    Email sendEmail(Email email);

    Email queueEmail(Email email);

    Optional<Email> findById(UUID emailId);

    Page<Email> findAll(Pageable pageable);

    Page<Email> findByStatus(EmailStatus status, Pageable pageable);

    int processQueuedEmails(int batchSize);

    int retryFailedEmails(int maxRetryCount, int batchSize);

    long countByStatus(EmailStatus status);
}
