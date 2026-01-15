package com.ecclesiaflow.communication.application.messaging;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.services.EmailService;
import com.ecclesiaflow.grpc.email.EmailQueueMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailMessageListenerTest {

    @Mock
    private EmailService emailService;

    private EmailQueueMessage buildMessage(UUID id) {
        return EmailQueueMessage.newBuilder()
                .setEmailId(id.toString())
                .build();
    }

    @Test
    void handleEmailMessage_shouldSendWhenFound() {
        EmailMessageListener listener = new EmailMessageListener(emailService);
        UUID id = UUID.randomUUID();
        Email email = Email.builder().id(id).build();
        EmailQueueMessage message = buildMessage(id);

        when(emailService.findById(id)).thenReturn(Optional.of(email));

        listener.handleEmailMessage(message);

        verify(emailService).sendEmail(email);
    }

    @Test
    void handleEmailMessage_shouldNotSendWhenNotFound() {
        EmailMessageListener listener = new EmailMessageListener(emailService);
        UUID id = UUID.randomUUID();
        EmailQueueMessage message = buildMessage(id);

        when(emailService.findById(id)).thenReturn(Optional.empty());

        listener.handleEmailMessage(message);

        verify(emailService, never()).sendEmail(any());
    }

    @Test
    void handleRetryMessage_shouldIncrementAndSendWhenBelowLimit() {
        EmailMessageListener listener = new EmailMessageListener(emailService);
        UUID id = UUID.randomUUID();
        Email email = Email.builder().id(id).retryCount(2).build();
        EmailQueueMessage message = buildMessage(id);

        when(emailService.findById(id)).thenReturn(Optional.of(email));

        listener.handleRetryMessage(message);

        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        verify(emailService).sendEmail(captor.capture());
        Email sent = captor.getValue();
        assertThat(sent.getRetryCount()).isEqualTo(3);
    }

    @Test
    void handleRetryMessage_shouldNotSendWhenAtOrAboveLimit() {
        EmailMessageListener listener = new EmailMessageListener(emailService);
        UUID id = UUID.randomUUID();
        Email email = Email.builder().id(id).retryCount(3).build();
        EmailQueueMessage message = buildMessage(id);

        when(emailService.findById(id)).thenReturn(Optional.of(email));

        listener.handleRetryMessage(message);

        verify(emailService, never()).sendEmail(any());
    }
}
