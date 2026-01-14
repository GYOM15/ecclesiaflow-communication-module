package com.ecclesiaflow.communication.business.domain.email;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Email domain model representing an communication in the business layer.
 * <p>
 * This is a pure business object with no JPA annotations or framework dependencies.
 * It represents the core concept of an communication in the EcclesiaFlow system.
 * </p>
 *
 * <p><strong>Immutability:</strong></p>
 * <p>
 * Uses Lombok @Builder for immutable construction. All fields are final.
 * State changes create new instances (functional programming style).
 * </p>
 *
 * <p><strong>Validation:</strong></p>
 * <p>
 * Business rules are enforced in the service layer, not in the domain model.
 * This keeps the model anemic but testable and framework-independent.
 * </p>
 *
 * @author EcclesiaFlow Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@ToString
public class Email {

    /**
     * Unique communication identifier (UUID).
     */
    private UUID id;

    /**
     * Recipient communication addresses.
     */
    private List<String> toAddresses;

    /**
     * Sender communication address.
     */
    private String fromAddress;

    /**
     * Email subject line.
     */
    private String subject;

    /**
     * Template name used for this communication (ex: "emails/password-reset").
     */
    private String templateName;

    /**
     * Template variables (key-value pairs).
     */
    private Map<String, Object> variables;

    /**
     * Email priority for queue processing.
     */
    private EmailPriority priority;

    /**
     * Current communication status.
     */
    private EmailStatus status;

    /**
     * Provider used to send the communication (gmail, sendgrid, aws-ses).
     */
    private String provider;

    /**
     * Provider-specific message ID for tracking.
     */
    private String messageId;

    /**
     * Timestamp when communication was queued.
     */
    private LocalDateTime queuedAt;

    /**
     * Timestamp when communication was sent to provider.
     */
    private LocalDateTime sentAt;

    /**
     * Timestamp when communication was delivered to recipient.
     */
    private LocalDateTime deliveredAt;

    /**
     * Timestamp when communication failed to send.
     */
    private LocalDateTime failedAt;

    /**
     * Error message if communication failed.
     */
    private String errorMessage;

    /**
     * Number of retry attempts.
     */
    private int retryCount;

    /**
     * Creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp.
     */
    private LocalDateTime updatedAt;

    // ========================================================================
    // BUSINESS METHODS
    // ========================================================================

    /**
     * Marks the communication as sent.
     *
     * @param provider the provider used to send
     * @param messageId the provider's message ID
     * @return new Email instance with updated status
     */
    public Email markAsSent(String provider, String messageId) {
        return this.toBuilder()
                .status(EmailStatus.SENT)
                .provider(provider)
                .messageId(messageId)
                .sentAt(LocalDateTime.now())
                .build();
    }

    /**
     * Marks the communication as delivered.
     *
     * @return new Email instance with updated status
     */
    public Email markAsDelivered() {
        return this.toBuilder()
                .status(EmailStatus.DELIVERED)
                .deliveredAt(LocalDateTime.now())
                .build();
    }

    /**
     * Marks the communication as failed.
     *
     * @param errorMessage the error message
     * @return new Email instance with updated status
     */
    public Email markAsFailed(String errorMessage) {
        return this.toBuilder()
                .status(EmailStatus.FAILED)
                .failedAt(LocalDateTime.now())
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Marks the communication as bounced.
     *
     * @param errorMessage the bounce reason
     * @return new Email instance with updated status
     */
    public Email markAsBounced(String errorMessage) {
        return this.toBuilder()
                .status(EmailStatus.BOUNCED)
                .failedAt(LocalDateTime.now())
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Increments the retry count.
     *
     * @return new Email instance with incremented retry count
     */
    public Email incrementRetryCount() {
        return this.toBuilder()
                .retryCount(this.retryCount + 1)
                .build();
    }

    /**
     * Checks if the communication can be retried based on max attempts.
     *
     * @param maxAttempts maximum retry attempts allowed
     * @return true if retry is allowed, false otherwise
     */
    public boolean canRetry(int maxAttempts) {
        return this.retryCount < maxAttempts && this.status == EmailStatus.FAILED;
    }

    /**
     * Checks if the communication is in a final state (no further processing).
     *
     * @return true if status is DELIVERED, FAILED, or BOUNCED
     */
    public boolean isFinalState() {
        return status == EmailStatus.DELIVERED || 
               status == EmailStatus.FAILED || 
               status == EmailStatus.BOUNCED;
    }
}
