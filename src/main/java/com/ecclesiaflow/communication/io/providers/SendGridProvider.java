package com.ecclesiaflow.communication.io.providers;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.provider.EmailProvider;
import com.ecclesiaflow.communication.business.domain.provider.SendResult;
import com.ecclesiaflow.communication.business.exceptions.EmailSendingException;
import com.ecclesiaflow.communication.business.services.TemplateService;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Email provider implementation via SendGrid.
 * @see EmailProvider
 * @see SendGrid
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(name = "sendgrid.enabled", havingValue = "true", matchIfMissing = false)
public class SendGridProvider implements EmailProvider {
    
    private final TemplateService templateService;
    
    @Value("${sendgrid.api.key}")
    private String apiKey;
    
    @Setter
    private java.util.function.Function<String, SendGrid> sendGridFactory = SendGrid::new;
    
    public SendGridProvider(TemplateService templateService) {
        this.templateService = templateService;
    }
    
    @Override
    public SendResult send(Email email) {
        try {
            SendGrid sg = sendGridFactory.apply(apiKey);
            
            // Render template
            String htmlContent = templateService.renderTemplate(
                email.getTemplateName(),
                email.getVariables()
            );
            
            // Build communication
            com.sendgrid.helpers.mail.objects.Email from = 
                new com.sendgrid.helpers.mail.objects.Email(email.getFromAddress());
            
            com.sendgrid.helpers.mail.objects.Email to = 
                new com.sendgrid.helpers.mail.objects.Email(
                    email.getToAddresses().get(0)
                );
            
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, email.getSubject(), to, content);
            
            // Add tracking
            mail.setTrackingSettings(buildTrackingSettings());
            
            // Send
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                String messageId = extractMessageId(response);
                return SendResult.success(messageId, getProviderName());
            } else {
                return SendResult.failure(
                    "SendGrid error: " + response.getStatusCode(),
                    getProviderName()
                );
            }
            
        } catch (IOException e) {
            throw new EmailSendingException("SendGrid error: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }
    
    @Override
    public String getProviderName() {
        return "sendgrid";
    }
    
    @Override
    public int getPriority() {
        return 2; // Backup after Gmail
    }
    
    /**
     * Configures SendGrid tracking settings.
     */
    private com.sendgrid.helpers.mail.objects.TrackingSettings buildTrackingSettings() {
        com.sendgrid.helpers.mail.objects.TrackingSettings trackingSettings = 
            new com.sendgrid.helpers.mail.objects.TrackingSettings();
        
        // Click tracking
        com.sendgrid.helpers.mail.objects.ClickTrackingSetting clickTrackingSetting = 
            new com.sendgrid.helpers.mail.objects.ClickTrackingSetting();
        clickTrackingSetting.setEnable(true);
        clickTrackingSetting.setEnableText(true);
        trackingSettings.setClickTrackingSetting(clickTrackingSetting);
        
        // Open tracking
        com.sendgrid.helpers.mail.objects.OpenTrackingSetting openTrackingSetting = 
            new com.sendgrid.helpers.mail.objects.OpenTrackingSetting();
        openTrackingSetting.setEnable(true);
        trackingSettings.setOpenTrackingSetting(openTrackingSetting);
        
        return trackingSettings;
    }
    
    /**
     * Extracts message ID from SendGrid response.
     */
    private String extractMessageId(Response response) {
        String messageIdHeader = response.getHeaders().get("X-Message-Id");
        return messageIdHeader != null ? messageIdHeader : "sendgrid-" + System.currentTimeMillis();
    }
}
