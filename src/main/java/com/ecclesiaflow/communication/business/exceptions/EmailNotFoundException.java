package com.ecclesiaflow.communication.business.exceptions;

import java.util.UUID;

/**
 * Exception thrown when an communication is not found in the system.
 * <p>
 * This business exception is used when an communication lookup by ID fails,
 * providing clear error context for API responses.
 * </p>
 *
 * <p><strong>Architectural Role:</strong> Business Exception - Resource Not Found</p>
 *
 * @author EcclesiaFlow Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class EmailNotFoundException extends RuntimeException {

    private final UUID emailId;

    public EmailNotFoundException(UUID emailId) {
        super("Email not found: " + emailId);
        this.emailId = emailId;
    }

    public EmailNotFoundException(String message) {
        super(message);
        this.emailId = null;
    }

    public UUID getEmailId() {
        return emailId;
    }
}
