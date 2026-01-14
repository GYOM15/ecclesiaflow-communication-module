package com.ecclesiaflow.communication.web.exception;

import com.ecclesiaflow.communication.business.exceptions.EmailNotFoundException;
import com.ecclesiaflow.communication.business.exceptions.EmailSendingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @Test
    void handleEmailNotFoundException_shouldReturnNotFoundResponse() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/ecclesiaflow/emails/123");

        ResponseEntity<Object> entity = handler.handleEmailNotFoundException(
                new EmailNotFoundException(UUID.randomUUID()), request);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(entity.getBody()).isNotNull().isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) entity.getBody();
        assertThat(body).containsKeys("timestamp", "status", "error", "message", "path");
        assertThat(body)
                .extractingByKey("timestamp")
                .isNotNull()
                .isInstanceOf(OffsetDateTime.class);
        assertThat(body)
                .extractingByKey("status")
                .isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(body)
                .extractingByKey("path")
                .isEqualTo("/ecclesiaflow/emails/123");
    }

    @Test
    void handleEmailSendingException_shouldReturnInternalServerErrorResponse() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/ecclesiaflow/emails");

        ResponseEntity<Object> entity = handler.handleEmailSendingException(
                new EmailSendingException("Boom"), request);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(entity.getBody()).isNotNull().isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) entity.getBody();
        assertThat(body).containsKeys("timestamp", "status", "error", "message", "path");
        assertThat(body)
                .extractingByKey("status")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(body)
                .extractingByKey("message")
                .isEqualTo("Failed to send communication");
        assertThat(body)
                .extractingByKey("path")
                .isEqualTo("/ecclesiaflow/emails");
    }

    @Test
    void handleIllegalArgumentException_shouldReturnBadRequestResponse() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/ecclesiaflow/emails");

        ResponseEntity<Object> entity = handler.handleIllegalArgumentException(
                new IllegalArgumentException("invalid"), request);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(entity.getBody()).isNotNull().isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) entity.getBody();
        assertThat(body).containsKeys("timestamp", "status", "error", "message", "path");
        assertThat(body).extractingByKey("status").isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(body).extractingByKey("message").isEqualTo("invalid");
        assertThat(body).extractingByKey("path").isEqualTo("/ecclesiaflow/emails");
    }

    @Test
    void handleRuntimeException_shouldReturnInternalServerErrorResponse() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/ecclesiaflow/emails");

        ResponseEntity<Object> entity = handler.handleRuntimeException(
                new RuntimeException("boom"), request);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(entity.getBody()).isNotNull().isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) entity.getBody();
        assertThat(body).containsKeys("timestamp", "status", "error", "message", "path");
        assertThat(body).extractingByKey("status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(body).extractingByKey("message").isEqualTo("An unexpected error occurred");
        assertThat(body).extractingByKey("path").isEqualTo("/ecclesiaflow/emails");
    }

    @Test
    void handleGenericException_shouldReturnInternalServerErrorResponse() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/ecclesiaflow/emails");

        ResponseEntity<Object> entity = handler.handleGenericException(
                new Exception("boom"), request);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(entity.getBody()).isNotNull().isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) entity.getBody();
        assertThat(body).containsKeys("timestamp", "status", "error", "message", "path");
        assertThat(body).extractingByKey("status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(body).extractingByKey("message").isEqualTo("An error occurred while processing your request");
        assertThat(body).extractingByKey("path").isEqualTo("/ecclesiaflow/emails");
    }
}
