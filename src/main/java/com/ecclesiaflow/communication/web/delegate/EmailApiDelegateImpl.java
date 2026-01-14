package com.ecclesiaflow.communication.web.delegate;

import com.ecclesiaflow.communication.application.config.EmailConfigurationProperties;
import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.exceptions.EmailNotFoundException;
import com.ecclesiaflow.communication.business.services.EmailService;
import com.ecclesiaflow.communication.web.api.EmailApiDelegate;
import com.ecclesiaflow.communication.web.mappers.EmailDtoMapper;
import com.ecclesiaflow.communication.web.model.EmailStatusResponse;
import com.ecclesiaflow.communication.web.model.SendEmailRequest;
import com.ecclesiaflow.communication.web.model.SendEmailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * REST API delegate for communication operations.
 * Handles DTO conversion and delegates to {@link EmailService}.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class EmailApiDelegateImpl implements EmailApiDelegate {

    private final EmailService emailService;
    private final EmailDtoMapper emailDtoMapper;
    private final EmailConfigurationProperties emailConfig;

    @Override
    public ResponseEntity<SendEmailResponse> emailSend(SendEmailRequest sendEmailRequest) {
        Email email = emailDtoMapper.toDomain(sendEmailRequest);

        if (email.getFromAddress() == null || email.getFromAddress().isBlank()) {
            email = email.toBuilder()
                    .fromAddress(emailConfig.getFrom())
                    .build();
        }

        Email queuedEmail = emailService.queueEmail(email);
        SendEmailResponse response = emailDtoMapper.toSendEmailResponse(queuedEmail);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @Override
    public ResponseEntity<EmailStatusResponse> emailGetStatus(UUID emailId) {
        Email email = emailService.findById(emailId)
                .orElseThrow(() -> new EmailNotFoundException(emailId));

        EmailStatusResponse response = emailDtoMapper.toEmailStatusResponse(email);

        return ResponseEntity.ok(response);
    }
}
