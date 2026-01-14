package com.ecclesiaflow.communication.application.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * RabbitMQ message DTO for communication sending.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * Unique communication ID.
     */
    private UUID emailId;
    
    /**
     * Sender communication address.
     */
    private String from;
    
    /**
     * List of recipients.
     */
    private List<String> to;
    
    /**
     * Email subject.
     */
    private String subject;
    
    /**
     * Template name to use (e.g., "emails/password-reset").
     */
    private String templateName;
    
    /**
     * Variables for template rendering.
     */
    private Map<String, Object> variables;
    
    /**
     * Email priority.
     */
    private String priority;
    
    /**
     * Number of send attempts.
     */
    private int retryCount;
}
