package com.ecclesiaflow.communication.io.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Base entity class for all JPA entities in the Email Module.
 * <p>
 * Provides common fields and behavior:
 * </p>
 * <ul>
 *   <li><strong>id</strong> - UUID primary key</li>
 *   <li><strong>createdAt</strong> - Automatic creation timestamp</li>
 *   <li><strong>updatedAt</strong> - Automatic update timestamp</li>
 *   <li><strong>equals/hashCode</strong> - Based on business key (UUID)</li>
 * </ul>
 *
 * <p><strong>WHY NOT AbstractPersistable?</strong></p>
 * <ul>
 *   <li>❌ Couples domain to Spring Data framework</li>
 *   <li>❌ Violates Clean Architecture principles</li>
 *   <li>❌ equals/hashCode problematic for transient entities</li>
 *   <li>✅ BaseEntity is JPA-pure (@MappedSuperclass)</li>
 *   <li>✅ Provides full control over equality strategy</li>
 *   <li>✅ Can be easily tested and mocked</li>
 * </ul>
 *
 * <p><strong>Equality Strategy:</strong></p>
 * <p>
 * Uses UUID-based equality which is safe for both persistent and transient entities.
 * UUIDs are generated before persist(), ensuring stable identity.
 * </p>
 *
 * @author EcclesiaFlow Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    /**
     * Primary key - UUID for distributed systems compatibility.
     * Generated automatically before persist.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Creation timestamp - Automatically set by JPA auditing.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp - Automatically updated by JPA auditing.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Ensures createdAt and updatedAt are set before persist.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Ensures updatedAt is refreshed before update.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Equals based on business key (UUID).
     * <p>
     * Safe for both persistent and transient entities because UUID is generated
     * before persist via @GeneratedValue(strategy = GenerationType.UUID).
     * </p>
     *
     * @param o the object to compare
     * @return true if same UUID, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    /**
     * HashCode based on business key (UUID).
     * <p>
     * Stable across entity lifecycle because UUID doesn't change.
     * </p>
     *
     * @return hash code based on UUID
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * String representation for debugging.
     *
     * @return simple class name with ID
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + "}";
    }
}
