package com.ecclesiaflow.communication.business.domain.provider;

import com.ecclesiaflow.communication.business.domain.email.Email;

/**
 * Email provider abstraction (Strategy pattern).
 * Implementations: GmailSmtpProvider, SendGridProvider, AwsSesProvider.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public interface EmailProvider {

    SendResult send(Email email);

    boolean isAvailable();

    String getProviderName();

    default int getPriority() {
        return 100;
    }
}
