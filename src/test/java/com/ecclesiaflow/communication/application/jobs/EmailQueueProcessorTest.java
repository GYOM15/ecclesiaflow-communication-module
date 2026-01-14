package com.ecclesiaflow.communication.application.jobs;

import com.ecclesiaflow.communication.business.services.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailQueueProcessorTest {

    @Mock
    private EmailService emailService;

    @Test
    void processQueuedEmails_shouldDelegateToService() {
        EmailQueueProcessor processor = new EmailQueueProcessor(emailService);

        processor.processQueuedEmails();

        verify(emailService).processQueuedEmails(10);
    }

    @Test
    void retryFailedEmails_shouldDelegateToService() {
        EmailQueueProcessor processor = new EmailQueueProcessor(emailService);

        processor.retryFailedEmails();

        verify(emailService).retryFailedEmails(3, 5);
    }
}
