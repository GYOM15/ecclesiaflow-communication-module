package com.ecclesiaflow.communication.business.services;

import com.ecclesiaflow.grpc.email.EmailTemplateType;
import org.springframework.stereotype.Service;

/**
 * Email template resolution service.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Service
public class TemplateResolver {
    
    public String resolveTemplateName(EmailTemplateType templateType) {
        return switch (templateType) {
            case EMAIL_TEMPLATE_PASSWORD_RESET -> "emails/password-reset";
            case EMAIL_TEMPLATE_PASSWORD_CHANGED -> "emails/password-changed";
            case EMAIL_TEMPLATE_WELCOME -> "emails/welcome";
            case EMAIL_TEMPLATE_EMAIL_CONFIRMATION -> "emails/confirmation";
            case EMAIL_TEMPLATE_PROFILE_UPDATED -> "emails/profile-updated";
            case EMAIL_TEMPLATE_UNSPECIFIED, UNRECOGNIZED -> 
                throw new IllegalArgumentException("Invalid or unspecified template type: " + templateType);
        };
    }
    
    public String getDefaultSubject(EmailTemplateType templateType) {
        return switch (templateType) {
            case EMAIL_TEMPLATE_PASSWORD_RESET -> "Reset Your Password - EcclesiaFlow";
            case EMAIL_TEMPLATE_PASSWORD_CHANGED -> "Your Password Has Been Changed - EcclesiaFlow";
            case EMAIL_TEMPLATE_WELCOME -> "Welcome to EcclesiaFlow!";
            case EMAIL_TEMPLATE_EMAIL_CONFIRMATION -> "Confirm Your Email Address - EcclesiaFlow";
            case EMAIL_TEMPLATE_PROFILE_UPDATED -> "Your Profile Has Been Updated - EcclesiaFlow";
            case EMAIL_TEMPLATE_UNSPECIFIED, UNRECOGNIZED -> 
                throw new IllegalArgumentException("Invalid or unspecified template type: " + templateType);
        };
    }
    
    public boolean isSupported(EmailTemplateType templateType) {
        return templateType != null 
            && templateType != EmailTemplateType.EMAIL_TEMPLATE_UNSPECIFIED
            && templateType != EmailTemplateType.UNRECOGNIZED;
    }
}
