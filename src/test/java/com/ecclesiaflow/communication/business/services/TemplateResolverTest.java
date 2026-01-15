package com.ecclesiaflow.communication.business.services;

import com.ecclesiaflow.grpc.email.EmailTemplateType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateResolverTest {

    private final TemplateResolver resolver = new TemplateResolver();

    @Test
    void resolveTemplateName_shouldMapAllSupportedTypes() {
        assertThat(resolver.resolveTemplateName(EmailTemplateType.EMAIL_TEMPLATE_PASSWORD_RESET))
                .isEqualTo("emails/password-reset");
        assertThat(resolver.resolveTemplateName(EmailTemplateType.EMAIL_TEMPLATE_PASSWORD_CHANGED))
                .isEqualTo("emails/password-changed");
        assertThat(resolver.resolveTemplateName(EmailTemplateType.EMAIL_TEMPLATE_WELCOME))
                .isEqualTo("emails/welcome");
        assertThat(resolver.resolveTemplateName(EmailTemplateType.EMAIL_TEMPLATE_EMAIL_CONFIRMATION))
                .isEqualTo("emails/confirmation");
        assertThat(resolver.resolveTemplateName(EmailTemplateType.EMAIL_TEMPLATE_PROFILE_UPDATED))
                .isEqualTo("emails/profile-updated");
    }

    @Test
    void resolveTemplateName_shouldThrowForInvalidOrUnspecified() {
        assertThatThrownBy(() -> resolver.resolveTemplateName(EmailTemplateType.EMAIL_TEMPLATE_UNSPECIFIED))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> resolver.resolveTemplateName(EmailTemplateType.UNRECOGNIZED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getDefaultSubject_shouldReturnExpectedSubjects() {
        assertThat(resolver.getDefaultSubject(EmailTemplateType.EMAIL_TEMPLATE_WELCOME))
                .contains("Welcome");
        assertThat(resolver.getDefaultSubject(EmailTemplateType.EMAIL_TEMPLATE_PASSWORD_RESET))
                .contains("Reset");
        assertThat(resolver.getDefaultSubject(EmailTemplateType.EMAIL_TEMPLATE_PASSWORD_CHANGED))
                .contains("Changed");
        assertThat(resolver.getDefaultSubject(EmailTemplateType.EMAIL_TEMPLATE_EMAIL_CONFIRMATION))
                .contains("Confirm");
        assertThat(resolver.getDefaultSubject(EmailTemplateType.EMAIL_TEMPLATE_PROFILE_UPDATED))
                .contains("Profile");
    }

    @Test
    void getDefaultSubject_shouldThrowForInvalidOrUnspecified() {
        assertThatThrownBy(() -> resolver.getDefaultSubject(EmailTemplateType.EMAIL_TEMPLATE_UNSPECIFIED))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> resolver.getDefaultSubject(EmailTemplateType.UNRECOGNIZED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isSupported_shouldValidateTemplateTypes() {
        assertThat(resolver.isSupported(EmailTemplateType.EMAIL_TEMPLATE_WELCOME)).isTrue();
        assertThat(resolver.isSupported(EmailTemplateType.EMAIL_TEMPLATE_UNSPECIFIED)).isFalse();
        assertThat(resolver.isSupported(EmailTemplateType.UNRECOGNIZED)).isFalse();
        assertThat(resolver.isSupported(null)).isFalse();
    }
}
