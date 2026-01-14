package com.ecclesiaflow.communication.io.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Email Event JPA Entity - Tracks communication interactions.
 * <p>
 * Stores tracking events such as opens, clicks, bounces, etc.
 * </p>
 *
 * @author EcclesiaFlow Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Entity
@Table(name = "email_events", indexes = {
        @Index(name = "idx_event_email_id", columnList = "email_id"),
        @Index(name = "idx_event_type", columnList = "event_type"),
        @Index(name = "idx_event_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailEventEntity extends BaseEntity {

    @Column(name = "email_id", nullable = false)
    private UUID emailId;

    @Column(name = "event_type", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(name = "event_data", columnDefinition = "JSON")
    private String eventData; // Stored as JSON string

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    public enum EventType {
        OPENED, CLICKED, BOUNCED, DELIVERED, FAILED
    }
}
