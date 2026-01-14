package com.ecclesiaflow.communication.web.mappers;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.email.EmailPriority;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import com.ecclesiaflow.communication.web.model.EmailStatusResponse;
import com.ecclesiaflow.communication.web.model.SendEmailRequest;
import com.ecclesiaflow.communication.web.model.SendEmailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailDtoMapperImplTest {

    private EmailDtoMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new EmailDtoMapperImpl();
    }

    @Test
    void toSendEmailResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime queuedAt = LocalDateTime.now();
        Email email = Email.builder()
                .id(id)
                .status(EmailStatus.QUEUED)
                .queuedAt(queuedAt)
                .build();

        SendEmailResponse response = mapper.toSendEmailResponse(email);

        assertThat(response).isNotNull();
        assertThat(response.getEmailId()).isEqualTo(UUID.fromString(id.toString()));
        assertThat(response.getStatus()).isEqualTo(SendEmailResponse.StatusEnum.QUEUED);
        assertThat(response.getQueuedAt()).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Email processed successfully");
    }

    @Test
    void toSendEmailResponse_shouldReturnNullForNullInput() {
        assertThat(mapper.toSendEmailResponse(null)).isNull();
    }

    @Test
    void toSendEmailResponse_shouldHandleNullId() {
        Email email = Email.builder()
                .id(null)
                .status(EmailStatus.QUEUED)
                .queuedAt(LocalDateTime.now())
                .build();

        SendEmailResponse response = mapper.toSendEmailResponse(email);

        assertThat(response).isNotNull();
        assertThat(response.getEmailId()).isNull();
        assertThat(response.getStatus()).isEqualTo(SendEmailResponse.StatusEnum.QUEUED);
    }

    @Test
    void toDomain_shouldMapRequestFields() {
        SendEmailRequest request = new SendEmailRequest();
        request.setTo(List.of("test@example.com"));
        request.setSubject("Test Subject");
        request.setVariables(Map.of("key", "value"));
        request.setPriority(SendEmailRequest.PriorityEnum.HIGH);

        Email email = mapper.toDomain(request);

        assertThat(email).isNotNull();
        assertThat(email.getToAddresses()).containsExactly("test@example.com");
        assertThat(email.getSubject()).isEqualTo("Test Subject");
        assertThat(email.getVariables()).containsEntry("key", "value");
        assertThat(email.getPriority()).isEqualTo(EmailPriority.HIGH);
        assertThat(email.getStatus()).isEqualTo(EmailStatus.QUEUED);
        assertThat(email.getQueuedAt()).isNotNull();
        assertThat(email.getRetryCount()).isZero();
    }

    @Test
    void toDomain_shouldReturnNullForNullInput() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_shouldHandleNullListsAndMaps() {
        SendEmailRequest request = new SendEmailRequest();
        request.setTo(null);
        request.setSubject("Subject");
        request.setVariables(null);

        Email email = mapper.toDomain(request);

        assertThat(email).isNotNull();
        assertThat(email.getToAddresses()).isNull();
        assertThat(email.getVariables()).isNull();
    }

    @Test
    void toEmailStatusResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime queuedAt = LocalDateTime.now();
        LocalDateTime sentAt = LocalDateTime.now().plusMinutes(1);
        LocalDateTime deliveredAt = LocalDateTime.now().plusMinutes(2);
        LocalDateTime failedAt = LocalDateTime.now().plusMinutes(3);

        Email email = Email.builder()
                .id(id)
                .toAddresses(List.of("to@test.com"))
                .subject("Subject")
                .status(EmailStatus.SENT)
                .provider("gmail")
                .errorMessage("none")
                .queuedAt(queuedAt)
                .sentAt(sentAt)
                .deliveredAt(deliveredAt)
                .failedAt(failedAt)
                .build();

        EmailStatusResponse response = mapper.toEmailStatusResponse(email);

        assertThat(response).isNotNull();
        assertThat(response.getEmailId()).isEqualTo(UUID.fromString(id.toString()));
        assertThat(response.getTo()).containsExactly("to@test.com");
        assertThat(response.getSubject()).isEqualTo("Subject");
        assertThat(response.getStatus()).isEqualTo(EmailStatusResponse.StatusEnum.SENT);
        assertThat(response.getProvider()).isEqualTo("gmail");
        assertThat(response.getErrorMessage()).isEqualTo("none");
        assertThat(response.getQueuedAt()).isNotNull();
        assertThat(response.getSentAt()).isNotNull();
        assertThat(response.getDeliveredAt()).isNotNull();
        assertThat(response.getFailedAt()).isNotNull();
        assertThat(response.getClicks()).isZero();
    }

    @Test
    void toEmailStatusResponse_shouldReturnNullForNullInput() {
        assertThat(mapper.toEmailStatusResponse(null)).isNull();
    }

    @Test
    void toEmailStatusResponse_shouldHandleNullIdAndNullToAddresses() {
        Email email = Email.builder()
                .id(null)
                .toAddresses(null)
                .status(EmailStatus.QUEUED)
                .build();

        EmailStatusResponse response = mapper.toEmailStatusResponse(email);

        assertThat(response).isNotNull();
        assertThat(response.getEmailId()).isNull();
        assertThat(response.getTo()).isNull();
    }
}
