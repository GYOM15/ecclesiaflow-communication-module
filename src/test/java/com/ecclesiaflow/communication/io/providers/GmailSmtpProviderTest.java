package com.ecclesiaflow.communication.io.providers;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.provider.SendResult;
import com.ecclesiaflow.communication.business.exceptions.EmailSendingException;
import com.ecclesiaflow.communication.business.services.TemplateService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GmailSmtpProviderTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateService templateService;

    @Test
    void send_shouldReturnSuccess() {
        GmailSmtpProvider provider = new GmailSmtpProvider(mailSender, templateService);
        MimeMessage mimeMessage = new MimeMessage((Session) null);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateService.renderTemplate(eq("emails/test"), anyMap())).thenReturn("<html>ok</html>");

        Email email = Email.builder()
                .fromAddress("from@ecclesiaflow.com")
                .toAddresses(List.of("to@example.com"))
                .subject("Subject")
                .templateName("emails/test")
                .variables(Map.of("k", "v"))
                .build();

        SendResult result = provider.send(email);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProviderName()).isEqualTo("gmail");
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void send_shouldWrapMailException() {
        GmailSmtpProvider provider = new GmailSmtpProvider(mailSender, templateService);
        MimeMessage mimeMessage = new MimeMessage((Session) null);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateService.renderTemplate(anyString(), anyMap())).thenReturn("<html/>");
        doThrow(new MailSendException("boom")).when(mailSender).send(any(MimeMessage.class));

        Email email = Email.builder()
                .fromAddress("from@ecclesiaflow.com")
                .toAddresses(List.of("to@example.com"))
                .subject("Subject")
                .templateName("emails/test")
                .variables(Map.of())
                .build();

        assertThatThrownBy(() -> provider.send(email))
                .isInstanceOf(EmailSendingException.class)
                .hasMessageContaining("Gmail SMTP error");
    }

    @Test
    void isAvailable_shouldReflectCreateMimeMessageOutcome() {
        GmailSmtpProvider provider = new GmailSmtpProvider(mailSender, templateService);

        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        assertThat(provider.isAvailable()).isTrue();

        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("x"));
        assertThat(provider.isAvailable()).isFalse();
    }

    @Test
    void constants_shouldMatch() {
        GmailSmtpProvider provider = new GmailSmtpProvider(mailSender, templateService);
        assertThat(provider.getProviderName()).isEqualTo("gmail");
        assertThat(provider.getPriority()).isEqualTo(1);
    }
}
