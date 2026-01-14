package com.ecclesiaflow.communication.business.domain.email;

/**
 * Email delivery status enumeration.
 * <p>
 * Represents the lifecycle stages of an communication from queuing to final delivery.
 * </p>
 *
 * <p><strong>Status Flow:</strong></p>
 * <pre>
 * QUEUED → SENT → DELIVERED (success path)
 *   ↓       ↓
 * FAILED  BOUNCED (failure paths)
 * </pre>
 *
 * @author EcclesiaFlow Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public enum EmailStatus {
    /**
     * Email queued in RabbitMQ, waiting for processing.
     */
    QUEUED,

    /**
     * Email sent to SMTP server or API provider.
     */
    SENT,

    /**
     * Email delivered to recipient's inbox (confirmed by provider).
     */
    DELIVERED,

    /**
     * Email failed to send (SMTP error, API error, etc.).
     */
    FAILED,

    /**
     * Email bounced (hard bounce or soft bounce).
     */
    BOUNCED
}
