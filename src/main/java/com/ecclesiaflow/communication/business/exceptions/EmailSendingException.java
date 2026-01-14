package com.ecclesiaflow.communication.business.exceptions;

/**
 * Thrown when communication sending fails.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public class EmailSendingException extends RuntimeException {

    public EmailSendingException(String message) {
        super(message);
    }

    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
