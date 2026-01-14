package com.ecclesiaflow.communication.business.services.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThymeleafTemplateServiceTest {

    @Mock
    private TemplateEngine templateEngine;

    @Test
    void renderTemplate_shouldInjectLogoUrlWhenConfigured() {
        ThymeleafTemplateService service = new ThymeleafTemplateService(templateEngine, "https://example.com/logo.png");

        when(templateEngine.process(eq("emails/test"), any(Context.class))).thenReturn("rendered");

        String result = service.renderTemplate("emails/test", Map.of("firstName", "Jean"));

        assertThat(result).isEqualTo("rendered");

        ArgumentCaptor<Context> captor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("emails/test"), captor.capture());

        Context ctx = captor.getValue();
        assertThat(ctx).isNotNull();
        assertThat(ctx.getVariable("firstName")).isEqualTo("Jean");
        assertThat(ctx.getVariable("logoUrl")).isEqualTo("https://example.com/logo.png");
    }

    @Test
    void renderTemplate_shouldNotInjectLogoUrlWhenBlank() {
        ThymeleafTemplateService service = new ThymeleafTemplateService(templateEngine, "  ");

        when(templateEngine.process(eq("emails/test"), any(Context.class))).thenReturn("rendered");

        String result = service.renderTemplate("emails/test", Map.of("firstName", "Jean"));

        assertThat(result).isEqualTo("rendered");

        ArgumentCaptor<Context> captor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("emails/test"), captor.capture());

        Context ctx = captor.getValue();
        assertThat(ctx).isNotNull();
        assertThat(ctx.getVariable("firstName")).isEqualTo("Jean");
        assertThat(ctx.getVariable("logoUrl")).isNull();
    }

    @Test
    void renderTemplate_shouldNotInjectLogoUrlWhenNull() {
        ThymeleafTemplateService service = new ThymeleafTemplateService(templateEngine, null);

        when(templateEngine.process(eq("emails/test"), any(Context.class))).thenReturn("rendered");

        String result = service.renderTemplate("emails/test", Map.of("firstName", "Jean"));

        assertThat(result).isEqualTo("rendered");

        ArgumentCaptor<Context> captor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("emails/test"), captor.capture());

        Context ctx = captor.getValue();
        assertThat(ctx).isNotNull();
        assertThat(ctx.getVariable("firstName")).isEqualTo("Jean");
        assertThat(ctx.getVariable("logoUrl")).isNull();
    }

    @Test
    void renderTemplate_shouldHandleNullVariablesAndInjectLogo() {
        ThymeleafTemplateService service = new ThymeleafTemplateService(templateEngine, "https://example.com/logo.png");

        when(templateEngine.process(eq("emails/test"), any(Context.class))).thenReturn("rendered");

        String result = service.renderTemplate("emails/test", null);

        assertThat(result).isEqualTo("rendered");

        ArgumentCaptor<Context> captor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("emails/test"), captor.capture());

        Context ctx = captor.getValue();
        assertThat(ctx).isNotNull();
        assertThat(ctx.getVariable("logoUrl")).isEqualTo("https://example.com/logo.png");
        assertThat(ctx.getVariable("firstName")).isNull();
    }

    @Test
    void renderTemplate_shouldOverrideExistingLogoVariable() {
        ThymeleafTemplateService service = new ThymeleafTemplateService(templateEngine, "https://example.com/logo.png");

        when(templateEngine.process(eq("emails/test"), any(Context.class))).thenReturn("rendered");

        String result = service.renderTemplate("emails/test", Map.of("logoUrl", "http://wrong/logo.png"));

        assertThat(result).isEqualTo("rendered");

        ArgumentCaptor<Context> captor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("emails/test"), captor.capture());

        Context ctx = captor.getValue();
        assertThat(ctx).isNotNull();
        assertThat(ctx.getVariable("logoUrl")).isEqualTo("https://example.com/logo.png");
    }

    @Test
    void renderTemplateById_shouldDelegateToRenderTemplate() {
        ThymeleafTemplateService service = new ThymeleafTemplateService(templateEngine, "https://example.com/logo.png");

        when(templateEngine.process(eq("emails/test"), any(Context.class))).thenReturn("rendered");

        String result = service.renderTemplateById("emails/test", Map.of("k", "v"));

        assertThat(result).isEqualTo("rendered");
        verify(templateEngine).process(eq("emails/test"), any(Context.class));
    }

    @Test
    void templateExists_shouldReturnTrueWhenTemplateEngineProcesses() {
        ThymeleafTemplateService service = new ThymeleafTemplateService(templateEngine, "");
        when(templateEngine.process(eq("emails/test"), any(Context.class))).thenReturn("rendered");

        assertThat(service.templateExists("emails/test")).isTrue();
    }

    @Test
    void templateExists_shouldReturnFalseWhenTemplateEngineThrows() {
        ThymeleafTemplateService service = new ThymeleafTemplateService(templateEngine, "");
        when(templateEngine.process(eq("emails/missing"), any(Context.class))).thenThrow(new RuntimeException("missing"));

        assertThat(service.templateExists("emails/missing")).isFalse();
    }
}
