package com.ecclesiaflow.communication.io.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Template JPA Entity - Persistence layer representation.
 * <p>
 * Stores communication templates with Thymeleaf syntax and i18n support.
 * </p>
 *
 * @author EcclesiaFlow Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Entity
@Table(name = "templates", indexes = {
        @Index(name = "idx_template_name", columnList = "name"),
        @Index(name = "idx_template_category", columnList = "category"),
        @Index(name = "idx_template_locale", columnList = "locale"),
        @Index(name = "idx_template_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateEntity extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "subject", length = 500, nullable = false)
    private String subject;

    @Column(name = "html_content", columnDefinition = "TEXT", nullable = false)
    private String htmlContent;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "variables", columnDefinition = "JSON")
    private String variables; // Stored as JSON array

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "locale", length = 10, nullable = false)
    private String locale = "en";

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_by")
    private UUID createdBy;
}
