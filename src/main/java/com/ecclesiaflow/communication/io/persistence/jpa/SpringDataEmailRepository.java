package com.ecclesiaflow.communication.io.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for EmailEntity.
 * <p>
 * This is a low-level persistence interface that works with JPA entities.
 * It should NOT be used directly by business services.
 * Use {@code EmailRepository} (PORT) instead.
 * </p>
 *
 * @author EcclesiaFlow Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Repository
public interface SpringDataEmailRepository extends JpaRepository<EmailEntity, UUID> {

    /**
     * Find emails by status.
     *
     * @param status the communication status enum
     * @param pageable pagination parameters
     * @return page of communication entities
     */
    Page<EmailEntity> findByStatus(EmailEntity.EmailStatusEnum status, Pageable pageable);

    /**
     * Find emails by priority.
     *
     * @param priority the communication priority enum
     * @param pageable pagination parameters
     * @return page of communication entities
     */
    Page<EmailEntity> findByPriority(EmailEntity.EmailPriorityEnum priority, Pageable pageable);

    /**
     * Find emails by template name.
     *
     * @param templateName the template name (ex: "emails/password-reset")
     * @param pageable pagination parameters
     * @return page of communication entities
     */
    Page<EmailEntity> findByTemplateName(String templateName, Pageable pageable);

    /**
     * Find queued emails ordered by priority and queued time.
     * <p>
     * Uses custom query to order by priority (HIGH first) and then by queued time (oldest first).
     * </p>
     *
     * @param status must be QUEUED
     * @param pageable pagination with limit
     * @return list of queued emails ready for processing
     */
    @Query("SELECT e FROM EmailEntity e WHERE e.status = :status ORDER BY " +
           "CASE e.priority " +
           "WHEN 'HIGH' THEN 1 " +
           "WHEN 'NORMAL' THEN 2 " +
           "WHEN 'LOW' THEN 3 " +
           "END, e.queuedAt ASC")
    List<EmailEntity> findQueuedEmailsForProcessing(
            @Param("status") EmailEntity.EmailStatusEnum status,
            Pageable pageable
    );

    /**
     * Find failed emails eligible for retry.
     *
     * @param status must be FAILED
     * @param maxRetryCount maximum retry attempts allowed
     * @param pageable pagination with limit
     * @return list of failed emails that can be retried
     */
    @Query("SELECT e FROM EmailEntity e WHERE e.status = :status AND e.retryCount < :maxRetryCount ORDER BY e.failedAt ASC")
    List<EmailEntity> findFailedEmailsForRetry(
            @Param("status") EmailEntity.EmailStatusEnum status,
            @Param("maxRetryCount") int maxRetryCount,
            Pageable pageable
    );

    /**
     * Count emails by status.
     *
     * @param status the communication status
     * @return count of emails
     */
    long countByStatus(EmailEntity.EmailStatusEnum status);
}
