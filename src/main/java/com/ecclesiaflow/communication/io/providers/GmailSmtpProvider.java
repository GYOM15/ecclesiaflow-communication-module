package com.ecclesiaflow.communication.io.providers;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.provider.EmailProvider;
import com.ecclesiaflow.communication.business.domain.provider.SendResult;
import com.ecclesiaflow.communication.business.exceptions.EmailSendingException;
import com.ecclesiaflow.communication.business.services.TemplateService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Email provider implementation via Gmail SMTP.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class GmailSmtpProvider implements EmailProvider {

    private final JavaMailSender mailSender;
    private final TemplateService templateService;

    @Override
    public SendResult send(Email email) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(email.getFromAddress());
            helper.setTo(email.getToAddresses().toArray(new String[0]));
            helper.setSubject(email.getSubject());
            
            String htmlContent = templateService.renderTemplate(
                email.getTemplateName(),
                email.getVariables()
            );
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

            return SendResult.success(mimeMessage.getMessageID(), getProviderName());

        } catch (MessagingException | MailException e) {
            throw new EmailSendingException("Gmail SMTP error: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            mailSender.createMimeMessage();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "gmail";
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
