package com.ecclesiaflow.communication.business.domain.email;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Email repository port (interface) - Hexagonal Architecture.
 * <p>
 * This interface defines the contract for communication persistence operations.
 * It lives in the business layer and knows nothing about JPA or infrastructure.
 * </p>
 * <p>
 * Implementation is provided by {@code EmailRepositoryImpl} in the IO layer.
 * </p>
 *
 * @author EcclesiaFlow Team
 * @version 1.0.0
 * @since 2025-01-01
 * @see Email
 */
public interface EmailRepository {

    /**
     * Saves an communication (create or update).
     *
     * @param email the communication to save, non-null
     * @return the saved communication with updated fields
     * @throws IllegalArgumentException if communication is null
     */
    Email save(Email email);

    /**
     * Finds an communication by its ID.
     *
     * @param id the communication ID, non-null
     * @return an Optional containing the communication if found, empty otherwise
     * @throws IllegalArgumentException if id is null
     */
    Optional<Email> findById(UUID id);

    /**
     * Finds all emails with pagination.
     *
     * @param pageable pagination parameters, non-null
     * @return a page of emails
     * @throws IllegalArgumentException if pageable is null
     */
    Page<Email> findAll(Pageable pageable);

    /**
     * Finds emails by status with pagination.
     * <p>
     * <strong>⭐ Uses DTO Projection for performance</strong>
     * </p>
     *
     * @param status the communication status, non-null
     * @param pageable pagination parameters, non-null
     * @return a page of emails matching the status
     * @throws IllegalArgumentException if status or pageable is null
     */
    Page<Email> findByStatus(EmailStatus status, Pageable pageable);

    /**
     * Finds emails by priority with pagination.
     *
     * @param priority the communication priority, non-null
     * @param pageable pagination parameters, non-null
     * @return a page of emails matching the priority
     * @throws IllegalArgumentException if priority or pageable is null
     */
    Page<Email> findByPriority(EmailPriority priority, Pageable pageable);

    /**
     * Finds emails by template name with pagination.
     *
     * @param templateName the template name (ex: "emails/password-reset"), non-null
     * @param pageable pagination parameters, non-null
     * @return a page of emails using the specified template
     * @throws IllegalArgumentException if templateName or pageable is null
     */
    Page<Email> findByTemplateName(String templateName, Pageable pageable);

    /**
     * Finds queued emails ready for processing, ordered by priority and time.
     *
     * @param limit maximum number of emails to return
     * @return list of queued emails
     */
    List<Email> findQueuedEmails(int limit);

    /**
     * Finds failed emails eligible for retry.
     *
     * @param maxRetryCount maximum retry count
     * @param limit maximum number of emails to return
     * @return list of failed emails
     */
    List<Email> findFailedEmailsForRetry(int maxRetryCount, int limit);

    /**
     * Counts emails by status.
     *
     * @param status the communication status
     * @return count of emails
     */
    long countByStatus(EmailStatus status);

    /**
     * Deletes an communication.
     *
     * @param email the communication to delete
     */
    void delete(Email email);

    /**
     * Checks if an communication exists by ID.
     *
     * @param id the communication ID
     * @return true if communication exists
     */
    boolean existsById(UUID id);
}
