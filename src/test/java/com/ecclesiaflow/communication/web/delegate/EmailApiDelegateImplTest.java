package com.ecclesiaflow.communication.web.delegate;

import com.ecclesiaflow.communication.application.config.EmailConfigurationProperties;
import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.email.EmailPriority;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import com.ecclesiaflow.communication.business.exceptions.EmailNotFoundException;
import com.ecclesiaflow.communication.business.services.EmailService;
import com.ecclesiaflow.communication.web.mappers.EmailDtoMapper;
import com.ecclesiaflow.communication.web.model.EmailStatusResponse;
import com.ecclesiaflow.communication.web.model.SendEmailRequest;
import com.ecclesiaflow.communication.web.model.SendEmailResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailApiDelegateImplTest {

    @Mock
    private EmailService emailService;

    @Mock
    private EmailDtoMapper emailDtoMapper;

    @Test
    void emailSend_shouldApplyDefaultFromWhenMissing() {
        EmailConfigurationProperties config = new EmailConfigurationProperties();
        config.setFrom("noreply@ecclesiaflow.com");

        UUID emailId = UUID.randomUUID();


        EmailApiDelegateImpl delegate = new EmailApiDelegateImpl(emailService, emailDtoMapper, config);

        SendEmailRequest request = new SendEmailRequest();
        Email mapped = Email.builder()
                .toAddresses(List.of("test@example.com"))
                .fromAddress("  ")
                .subject("Subject")
                .templateName("emails/test")
                .variables(Map.of("k", "v"))
                .priority(EmailPriority.NORMAL)
                .status(EmailStatus.QUEUED)
                .build();

        Email queued = mapped.toBuilder().id(UUID.randomUUID()).build();
        SendEmailResponse response = new SendEmailResponse().emailId(emailId);

        when(emailDtoMapper.toDomain(request)).thenReturn(mapped);
        when(emailService.queueEmail(any(Email.class))).thenReturn(queued);
        when(emailDtoMapper.toSendEmailResponse(queued)).thenReturn(response);

        ResponseEntity<SendEmailResponse> entity = delegate.emailSend(request);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(entity.getBody()).isNotNull().isSameAs(response);

        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        verify(emailService).queueEmail(captor.capture());
        Email queuedEmail = captor.getValue();
        assertThat(queuedEmail).isNotNull();
        assertThat(queuedEmail.getFromAddress()).isEqualTo("noreply@ecclesiaflow.com");
    }

    @Test
    void emailSend_shouldNotOverrideFromWhenPresent() {
        EmailConfigurationProperties config = new EmailConfigurationProperties();
        config.setFrom("noreply@ecclesiaflow.com");

        EmailApiDelegateImpl delegate = new EmailApiDelegateImpl(emailService, emailDtoMapper, config);

        SendEmailRequest request = new SendEmailRequest();
        Email mapped = Email.builder()
                .toAddresses(List.of("test@example.com"))
                .fromAddress("sender@ecclesiaflow.com")
                .subject("Subject")
                .templateName("emails/test")
                .variables(Map.of("k", "v"))
                .build();

        Email queued = mapped.toBuilder().id(UUID.randomUUID()).build();
        SendEmailResponse response = new SendEmailResponse().emailId(queued.getId());

        when(emailDtoMapper.toDomain(request)).thenReturn(mapped);
        when(emailService.queueEmail(any(Email.class))).thenReturn(queued);
        when(emailDtoMapper.toSendEmailResponse(queued)).thenReturn(response);

        ResponseEntity<SendEmailResponse> entity = delegate.emailSend(request);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(entity.getBody()).isNotNull().isSameAs(response);

        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        verify(emailService).queueEmail(captor.capture());
        Email queuedEmail = captor.getValue();
        assertThat(queuedEmail).isNotNull();
        assertThat(queuedEmail.getFromAddress()).isEqualTo("sender@ecclesiaflow.com");
    }

    @Test
    void emailSend_shouldApplyDefaultFromWhenNull() {
        EmailConfigurationProperties config = new EmailConfigurationProperties();
        config.setFrom("noreply@ecclesiaflow.com");

        EmailApiDelegateImpl delegate = new EmailApiDelegateImpl(emailService, emailDtoMapper, config);

        SendEmailRequest request = new SendEmailRequest();
        Email mapped = Email.builder()
                .toAddresses(List.of("test@example.com"))
                .fromAddress(null)
                .subject("Subject")
                .templateName("emails/test")
                .variables(Map.of("k", "v"))
                .build();

        Email queued = mapped.toBuilder().id(UUID.randomUUID()).build();
        SendEmailResponse response = new SendEmailResponse().emailId(queued.getId());

        when(emailDtoMapper.toDomain(request)).thenReturn(mapped);
        when(emailService.queueEmail(any(Email.class))).thenReturn(queued);
        when(emailDtoMapper.toSendEmailResponse(queued)).thenReturn(response);

        ResponseEntity<SendEmailResponse> entity = delegate.emailSend(request);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(entity.getBody()).isNotNull().isSameAs(response);

        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        verify(emailService).queueEmail(captor.capture());
        Email queuedEmail = captor.getValue();
        assertThat(queuedEmail).isNotNull();
        assertThat(queuedEmail.getFromAddress()).isEqualTo("noreply@ecclesiaflow.com");
    }

    @Test
    void emailGetStatus_shouldReturnOkWhenFound() {
        EmailConfigurationProperties config = new EmailConfigurationProperties();
        EmailApiDelegateImpl delegate = new EmailApiDelegateImpl(emailService, emailDtoMapper, config);

        UUID emailId = UUID.randomUUID();
        Email email = Email.builder()
                .id(emailId)
                .toAddresses(List.of("test@example.com"))
                .fromAddress("noreply@ecclesiaflow.com")
                .subject("Subject")
                .templateName("emails/test")
                .variables(Map.of())
                .priority(EmailPriority.NORMAL)
                .status(EmailStatus.QUEUED)
                .build();

        EmailStatusResponse response = new EmailStatusResponse().emailId(emailId);

        when(emailService.findById(emailId)).thenReturn(Optional.of(email));
        when(emailDtoMapper.toEmailStatusResponse(email)).thenReturn(response);

        ResponseEntity<EmailStatusResponse> entity = delegate.emailGetStatus(emailId);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isNotNull().isSameAs(response);
    }

    @Test
    void emailGetStatus_shouldThrowWhenNotFound() {
        EmailConfigurationProperties config = new EmailConfigurationProperties();
        EmailApiDelegateImpl delegate = new EmailApiDelegateImpl(emailService, emailDtoMapper, config);

        UUID emailId = UUID.randomUUID();
        when(emailService.findById(emailId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> delegate.emailGetStatus(emailId))
                .isInstanceOf(EmailNotFoundException.class);
    }
}
