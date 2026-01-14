package com.ecclesiaflow.communication.business.services.impl;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.email.EmailPriority;
import com.ecclesiaflow.communication.business.domain.email.EmailRepository;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import com.ecclesiaflow.communication.business.domain.provider.EmailProvider;
import com.ecclesiaflow.communication.business.domain.provider.SendResult;
import com.ecclesiaflow.communication.business.exceptions.EmailSendingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EmailServiceImpl}.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private EmailProvider emailProvider;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        lenient().when(emailProvider.isAvailable()).thenReturn(true);
        lenient().when(emailProvider.getPriority()).thenReturn(1);
        emailService = new EmailServiceImpl(emailRepository, List.of(emailProvider));
    }

    @Test
    @DisplayName("queueEmail should save communication with QUEUED status")
    void queueEmail_shouldSaveWithQueuedStatus() {
        Email email = createTestEmail();
        Email savedEmail = email.toBuilder().id(UUID.randomUUID()).status(EmailStatus.QUEUED).build();

        when(emailRepository.save(any(Email.class))).thenReturn(savedEmail);

        Email result = emailService.queueEmail(email);

        assertThat(result.getStatus()).isEqualTo(EmailStatus.QUEUED);
        assertThat(result.getId()).isNotNull();
        verify(emailRepository).save(any(Email.class));
    }

    @Test
    @DisplayName("sendEmail should mark as SENT on success")
    void sendEmail_shouldMarkAsSentOnSuccess() {
        UUID emailId = UUID.randomUUID();
        Email email = createTestEmail().toBuilder().id(emailId).status(EmailStatus.QUEUED).build();
        SendResult successResult = SendResult.success("msg-123", "gmail");

        when(emailProvider.send(any(Email.class))).thenReturn(successResult);
        when(emailRepository.save(any(Email.class))).thenAnswer(inv -> inv.getArgument(0));

        Email result = emailService.sendEmail(email);

        assertThat(result.getStatus()).isEqualTo(EmailStatus.SENT);
        verify(emailProvider).send(any(Email.class));
    }

    @Test
    @DisplayName("sendEmail should mark as FAILED on provider failure")
    void sendEmail_shouldMarkAsFailedOnProviderFailure() {
        UUID emailId = UUID.randomUUID();
        Email email = createTestEmail().toBuilder().id(emailId).status(EmailStatus.QUEUED).build();
        SendResult failResult = SendResult.failure("Connection timeout", "gmail");

        when(emailProvider.send(any(Email.class))).thenReturn(failResult);
        when(emailRepository.save(any(Email.class))).thenAnswer(inv -> inv.getArgument(0));

        Email result = emailService.sendEmail(email);

        assertThat(result.getStatus()).isEqualTo(EmailStatus.FAILED);
    }

    @Test
    @DisplayName("sendEmail should queue and save communication when id is null")
    void sendEmail_shouldQueueAndSaveWhenIdIsNull() {
        Email email = createTestEmail().toBuilder().id(null).build();

        when(emailRepository.save(any(Email.class))).thenAnswer(inv -> {
            Email arg = inv.getArgument(0);
            return arg.toBuilder().id(UUID.randomUUID()).build();
        });
        when(emailProvider.send(any(Email.class))).thenReturn(SendResult.success("msg-123", "gmail"));

        Email result = emailService.sendEmail(email);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(EmailStatus.SENT);
        assertThat(result.getQueuedAt()).isNotNull();
        verify(emailRepository, atLeastOnce()).save(any(Email.class));
        verify(emailProvider).send(any(Email.class));
    }

    @Test
    @DisplayName("sendEmail should mark as FAILED when provider throws EmailSendingException")
    void sendEmail_shouldMarkAsFailedWhenProviderThrowsEmailSendingException() {
        UUID emailId = UUID.randomUUID();
        Email email = createTestEmail().toBuilder().id(emailId).status(EmailStatus.QUEUED).build();

        when(emailProvider.send(any(Email.class))).thenThrow(new EmailSendingException("timeout"));
        when(emailRepository.save(any(Email.class))).thenAnswer(inv -> inv.getArgument(0));

        Email result = emailService.sendEmail(email);

        assertThat(result.getStatus()).isEqualTo(EmailStatus.FAILED);
        assertThat(result.getFailedAt()).isNotNull();
        assertThat(result.getErrorMessage()).isEqualTo("timeout");
    }

    @Test
    @DisplayName("sendEmail should queue communication when no provider available")
    void sendEmail_shouldQueueWhenNoProviderAvailable() {
        when(emailProvider.isAvailable()).thenReturn(false);

        when(emailRepository.save(any(Email.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Email email = createTestEmail();

        Email result = emailService.sendEmail(email);

        assertThat(result.getStatus()).isEqualTo(EmailStatus.QUEUED);
        assertThat(result.getQueuedAt()).isNotNull();

        verify(emailRepository).save(any(Email.class));
        verify(emailProvider, never()).send(any());
    }


    @Test
    @DisplayName("sendEmail should queue and save when provider unavailable and communication has id")
    void sendEmail_shouldQueueAndSaveWhenProviderUnavailableAndHasId() {
        when(emailProvider.isAvailable()).thenReturn(false);

        when(emailRepository.save(any(Email.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Email email = createTestEmail().toBuilder()
                .id(UUID.randomUUID())
                .build();

        Email result = emailService.sendEmail(email);

        assertThat(result.getStatus()).isEqualTo(EmailStatus.QUEUED);
        assertThat(result.getQueuedAt()).isNotNull();

        verify(emailRepository).save(any(Email.class));
        verify(emailProvider, never()).send(any());
    }


    @Test
    @DisplayName("findById should return communication when exists")
    void findById_shouldReturnEmailWhenExists() {
        UUID emailId = UUID.randomUUID();
        Email email = createTestEmail().toBuilder().id(emailId).build();

        when(emailRepository.findById(emailId)).thenReturn(Optional.of(email));

        Optional<Email> result = emailService.findById(emailId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(emailId);
    }

    @Test
    @DisplayName("findById should return empty when not exists")
    void findById_shouldReturnEmptyWhenNotExists() {
        UUID emailId = UUID.randomUUID();

        when(emailRepository.findById(emailId)).thenReturn(Optional.empty());

        Optional<Email> result = emailService.findById(emailId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll should delegate to repository")
    void findAll_shouldDelegateToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Email> page = new PageImpl<>(List.of(createTestEmail()));
        when(emailRepository.findAll(pageable)).thenReturn(page);

        Page<Email> result = emailService.findAll(pageable);

        assertThat(result).isSameAs(page);
        verify(emailRepository).findAll(pageable);
    }

    @Test
    @DisplayName("findByStatus should delegate to repository")
    void findByStatus_shouldDelegateToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Email> page = new PageImpl<>(List.of(createTestEmail().toBuilder().status(EmailStatus.QUEUED).build()));
        when(emailRepository.findByStatus(EmailStatus.QUEUED, pageable)).thenReturn(page);

        Page<Email> result = emailService.findByStatus(EmailStatus.QUEUED, pageable);

        assertThat(result).isSameAs(page);
        verify(emailRepository).findByStatus(EmailStatus.QUEUED, pageable);
    }

    @Test
    @DisplayName("countByStatus should delegate to repository")
    void countByStatus_shouldDelegateToRepository() {
        when(emailRepository.countByStatus(EmailStatus.SENT)).thenReturn(5L);

        long result = emailService.countByStatus(EmailStatus.SENT);

        assertThat(result).isEqualTo(5L);
        verify(emailRepository).countByStatus(EmailStatus.SENT);
    }

    @Test
    @DisplayName("processQueuedEmails should fetch queued emails and process them")
    void processQueuedEmails_shouldFetchAndProcessBatch() {
        Email email1 = createTestEmail().toBuilder().id(UUID.randomUUID()).status(EmailStatus.QUEUED).build();
        Email email2 = createTestEmail().toBuilder().id(UUID.randomUUID()).status(EmailStatus.QUEUED).build();

        when(emailRepository.findQueuedEmails(10)).thenReturn(List.of(email1, email2));
        when(emailProvider.send(any(Email.class))).thenReturn(SendResult.success("msg", "gmail"));
        when(emailRepository.save(any(Email.class))).thenAnswer(inv -> inv.getArgument(0));

        int processed = emailService.processQueuedEmails(10);

        assertThat(processed).isEqualTo(2);
        verify(emailRepository).findQueuedEmails(10);
        verify(emailProvider, times(2)).send(any(Email.class));
    }

    @Test
    @DisplayName("processQueuedEmails should continue on runtime exception and count only successes")
    void processQueuedEmails_shouldContinueOnRuntimeException() {
        Email email1 = createTestEmail().toBuilder().id(UUID.randomUUID()).status(EmailStatus.QUEUED).build();
        Email email2 = createTestEmail().toBuilder().id(UUID.randomUUID()).status(EmailStatus.QUEUED).build();

        when(emailRepository.findQueuedEmails(2)).thenReturn(List.of(email1, email2));
        when(emailRepository.save(any(Email.class))).thenAnswer(inv -> inv.getArgument(0));

        when(emailProvider.send(any(Email.class)))
                .thenThrow(new RuntimeException("boom"))
                .thenReturn(SendResult.success("msg", "gmail"));

        int processed = emailService.processQueuedEmails(2);

        assertThat(processed).isEqualTo(1);
        verify(emailRepository).findQueuedEmails(2);
        verify(emailProvider, times(2)).send(any(Email.class));
    }

    @Test
    @DisplayName("retryFailedEmails should fetch failed emails, increment retryCount, and process them")
    void retryFailedEmails_shouldIncrementRetryCountAndProcess() {
        Email failedEmail = createTestEmail().toBuilder()
                .id(UUID.randomUUID())
                .status(EmailStatus.FAILED)
                .retryCount(1)
                .build();

        when(emailRepository.findFailedEmailsForRetry(3, 5)).thenReturn(List.of(failedEmail));
        when(emailProvider.send(any(Email.class))).thenReturn(SendResult.success("msg", "gmail"));
        when(emailRepository.save(any(Email.class))).thenAnswer(inv -> inv.getArgument(0));

        int processed = emailService.retryFailedEmails(3, 5);

        assertThat(processed).isEqualTo(1);

        verify(emailRepository).findFailedEmailsForRetry(3, 5);

        // ensure the communication sent to provider has retryCount incremented
        var emailCaptor = org.mockito.ArgumentCaptor.forClass(Email.class);
        verify(emailProvider).send(emailCaptor.capture());
        assertThat(emailCaptor.getValue()).isNotNull();
        assertThat(emailCaptor.getValue().getRetryCount()).isEqualTo(2);
    }

    private Email createTestEmail() {
        return Email.builder()
                .toAddresses(List.of("test@example.com"))
                .subject("Test Subject")
                .templateName("emails/test")
                .variables(Map.of("key", "value"))
                .fromAddress("sender@ecclesiaflow.com")
                .priority(EmailPriority.NORMAL)
                .build();
    }
}
