package com.ecclesiaflow.communication.io.persistence.projections;

import com.ecclesiaflow.communication.io.persistence.jpa.EmailEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Email summary projection for optimized read queries.
 * <p>
 * Interface projection that selects only necessary fields for list views.
 * </p>
 *
 * @author EcclesiaFlow Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public interface EmailSummaryProjection {

    /**
     * Email unique identifier.
     */
    UUID getId();

    /**
     * Recipient communication addresses (comma-separated).
     */
    String getToAddresses();

    /**
     * Email subject line.
     */
    String getSubject();

    /**
     * Current communication status.
     */
    EmailEntity.EmailStatusEnum getStatus();

    /**
     * Email priority.
     */
    EmailEntity.EmailPriorityEnum getPriority();

    /**
     * Provider used to send (gmail, sendgrid, aws-ses).
     */
    String getProvider();

    /**
     * Timestamp when communication was queued.
     */
    LocalDateTime getQueuedAt();

    /**
     * Timestamp when communication was sent.
     */
    LocalDateTime getSentAt();

    /**
     * Timestamp when communication was delivered.
     */
    LocalDateTime getDeliveredAt();

    /**
     * Number of retry attempts.
     */
    int getRetryCount();
}
