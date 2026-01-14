package com.ecclesiaflow.communication.io.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Email JPA Entity - Persistence layer representation.
 * <p>
 * This entity is part of the IO layer and knows about JPA/Hibernate.
 * It is mapped to/from the domain model {@code Email} by {@code EmailPersistenceMapper}.
 * </p>
 *
 * @author EcclesiaFlow Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Entity
@Table(name = "emails", indexes = {
        @Index(name = "idx_email_status", columnList = "status"),
        @Index(name = "idx_email_queued_at", columnList = "queued_at"),
        @Index(name = "idx_email_template_name", columnList = "template_name"),
        @Index(name = "idx_email_priority", columnList = "priority"),
        @Index(name = "idx_email_provider", columnList = "provider")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailEntity extends BaseEntity {

    @Column(name = "to_addresses", columnDefinition = "TEXT", nullable = false)
    private String toAddresses; // Stored as comma-separated string

    @Column(name = "from_address", nullable = false)
    private String fromAddress;

    @Column(name = "subject", length = 500, nullable = false)
    private String subject;

    @Column(name = "template_name", length = 100)
    private String templateName;

    @Column(name = "variables", columnDefinition = "JSON")
    private String variables; // Stored as JSON string

    @Column(name = "priority", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private EmailPriorityEnum priority;

    @Column(name = "status", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private EmailStatusEnum status;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "queued_at", nullable = false)
    private LocalDateTime queuedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    public enum EmailStatusEnum {
        QUEUED, SENT, DELIVERED, FAILED, BOUNCED
    }

    public enum EmailPriorityEnum {
        HIGH, NORMAL, LOW
    }
}
