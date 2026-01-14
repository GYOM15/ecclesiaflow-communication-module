package com.ecclesiaflow.communication.io.providers;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.provider.SendResult;
import com.ecclesiaflow.communication.business.exceptions.EmailSendingException;
import com.ecclesiaflow.communication.business.services.TemplateService;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendGridProviderTest {

    @Mock
    private TemplateService templateService;

    @Mock
    private SendGrid sendGrid;

    private SendGridProvider provider;

    @BeforeEach
    void setUp() throws Exception {
        provider = new SendGridProvider(templateService);
        setApiKey("SG.test-key");
        provider.setSendGridFactory(apiKey -> sendGrid);
    }

    private void setApiKey(String value) throws Exception {
        Field f = SendGridProvider.class.getDeclaredField("apiKey");
        f.setAccessible(true);
        f.set(provider, value);
    }

    private Email buildTestEmail() {
        return Email.builder()
                .fromAddress("from@test.com")
                .toAddresses(List.of("to@test.com"))
                .subject("Test")
                .templateName("emails/welcome")
                .variables(Map.of("name", "John"))
                .build();
    }

    @Test
    void isAvailable_shouldReturnFalseWhenApiKeyIsNull() throws Exception {
        setApiKey(null);
        assertThat(provider.isAvailable()).isFalse();
    }

    @Test
    void isAvailable_shouldReturnFalseWhenApiKeyIsBlank() throws Exception {
        setApiKey(" ");
        assertThat(provider.isAvailable()).isFalse();
    }

    @Test
    void isAvailable_shouldReturnTrueWhenApiKeyIsSet() {
        assertThat(provider.isAvailable()).isTrue();
    }

    @Test
    void constants_shouldMatch() {
        assertThat(provider.getProviderName()).isEqualTo("sendgrid");
        assertThat(provider.getPriority()).isEqualTo(2);
    }

    @Test
    void send_shouldReturnSuccessOnStatusCode202() throws Exception {
        when(templateService.renderTemplate(any(), any())).thenReturn("<html>body</html>");

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        mockResponse.setHeaders(Map.of("X-Message-Id", "msg-123"));
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        SendResult result = provider.send(buildTestEmail());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessageId()).isEqualTo("msg-123");
        assertThat(result.getProviderName()).isEqualTo("sendgrid");
    }

    @Test
    void send_shouldReturnSuccessOnStatusCode200() throws Exception {
        when(templateService.renderTemplate(any(), any())).thenReturn("<html>body</html>");

        Response mockResponse = new Response();
        mockResponse.setStatusCode(200);
        mockResponse.setHeaders(Map.of("X-Message-Id", "msg-200"));
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        SendResult result = provider.send(buildTestEmail());

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void send_shouldReturnSuccessOnStatusCode299() throws Exception {
        when(templateService.renderTemplate(any(), any())).thenReturn("<html>body</html>");

        Response mockResponse = new Response();
        mockResponse.setStatusCode(299);
        mockResponse.setHeaders(Map.of("X-Message-Id", "msg-299"));
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        SendResult result = provider.send(buildTestEmail());

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void send_shouldReturnFailureOnStatusCode300() throws Exception {
        when(templateService.renderTemplate(any(), any())).thenReturn("<html>body</html>");

        Response mockResponse = new Response();
        mockResponse.setStatusCode(300);
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        SendResult result = provider.send(buildTestEmail());

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("300");
    }

    @Test
    void send_shouldReturnFailureOnStatusCode400() throws Exception {
        when(templateService.renderTemplate(any(), any())).thenReturn("<html>body</html>");

        Response mockResponse = new Response();
        mockResponse.setStatusCode(400);
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        SendResult result = provider.send(buildTestEmail());

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("400");
    }

    @Test
    void send_shouldReturnFailureOnStatusCode199() throws Exception {
        when(templateService.renderTemplate(any(), any())).thenReturn("<html>body</html>");

        Response mockResponse = new Response();
        mockResponse.setStatusCode(199);
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        SendResult result = provider.send(buildTestEmail());

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("199");
    }

    @Test
    void send_shouldThrowOnIOException() throws Exception {
        when(templateService.renderTemplate(any(), any())).thenReturn("<html>body</html>");
        when(sendGrid.api(any(Request.class))).thenThrow(new IOException("Network error"));

        assertThatThrownBy(() -> provider.send(buildTestEmail()))
                .isInstanceOf(EmailSendingException.class)
                .hasMessageContaining("SendGrid error");
    }

    @Test
    void send_shouldFallbackMessageIdWhenHeaderMissing() throws Exception {
        when(templateService.renderTemplate(any(), any())).thenReturn("<html>body</html>");

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);
        mockResponse.setHeaders(Map.of());
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        SendResult result = provider.send(buildTestEmail());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessageId()).startsWith("sendgrid-");
    }
}
