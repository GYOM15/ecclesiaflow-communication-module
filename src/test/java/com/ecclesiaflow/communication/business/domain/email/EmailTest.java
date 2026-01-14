package com.ecclesiaflow.communication.business.domain.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Email} domain entity.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
class EmailTest {

    @Test
    @DisplayName("markAsSent should update status and timestamps")
    void markAsSent_shouldUpdateStatusAndTimestamps() {
        Email email = createTestEmail();

        Email sentEmail = email.markAsSent("gmail", "msg-123");

        assertThat(sentEmail.getStatus()).isEqualTo(EmailStatus.SENT);
        assertThat(sentEmail.getSentAt()).isNotNull();
        assertThat(sentEmail.getProvider()).isEqualTo("gmail");
        assertThat(sentEmail.getMessageId()).isEqualTo("msg-123");
    }

    @Test
    @DisplayName("markAsFailed should update status and error message")
    void markAsFailed_shouldUpdateStatusAndErrorMessage() {
        Email email = createTestEmail();

        Email failedEmail = email.markAsFailed("Connection timeout");

        assertThat(failedEmail.getStatus()).isEqualTo(EmailStatus.FAILED);
        assertThat(failedEmail.getFailedAt()).isNotNull();
        assertThat(failedEmail.getErrorMessage()).isEqualTo("Connection timeout");
    }

    @Test
    @DisplayName("markAsDelivered should update status and deliveredAt")
    void markAsDelivered_shouldUpdateStatusAndDeliveredAt() {
        Email email = createTestEmail();

        Email deliveredEmail = email.markAsDelivered();

        assertThat(deliveredEmail.getStatus()).isEqualTo(EmailStatus.DELIVERED);
        assertThat(deliveredEmail.getDeliveredAt()).isNotNull();
    }

    @Test
    @DisplayName("markAsBounced should update status and failedAt")
    void markAsBounced_shouldUpdateStatusAndFailedAt() {
        Email email = createTestEmail();

        Email bouncedEmail = email.markAsBounced("Mailbox unavailable");

        assertThat(bouncedEmail.getStatus()).isEqualTo(EmailStatus.BOUNCED);
        assertThat(bouncedEmail.getFailedAt()).isNotNull();
        assertThat(bouncedEmail.getErrorMessage()).isEqualTo("Mailbox unavailable");
    }

    @Test
    @DisplayName("incrementRetryCount should increase retry counter")
    void incrementRetryCount_shouldIncreaseCounter() {
        Email email = createTestEmail().toBuilder().retryCount(2).build();

        Email retriedEmail = email.incrementRetryCount();

        assertThat(retriedEmail.getRetryCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("canRetry should be true only for FAILED status and retryCount below max")
    void canRetry_shouldFollowBusinessRule() {
        Email failed = createTestEmail().toBuilder()
                .status(EmailStatus.FAILED)
                .retryCount(1)
                .build();

        assertThat(failed.canRetry(3)).isTrue();
        assertThat(failed.canRetry(1)).isFalse();

        Email notFailed = failed.toBuilder().status(EmailStatus.QUEUED).build();
        assertThat(notFailed.canRetry(3)).isFalse();
    }

    @Test
    @DisplayName("isFinalState should be true for DELIVERED/FAILED/BOUNCED")
    void isFinalState_shouldMatchStatuses() {
        assertThat(createTestEmail().toBuilder().status(EmailStatus.DELIVERED).build().isFinalState()).isTrue();
        assertThat(createTestEmail().toBuilder().status(EmailStatus.FAILED).build().isFinalState()).isTrue();
        assertThat(createTestEmail().toBuilder().status(EmailStatus.BOUNCED).build().isFinalState()).isTrue();
        assertThat(createTestEmail().toBuilder().status(EmailStatus.QUEUED).build().isFinalState()).isFalse();
    }

    @Test
    @DisplayName("builder should create valid communication")
    void builder_shouldCreateValidEmail() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Email email = Email.builder()
                .id(id)
                .toAddresses(List.of("test@example.com"))
                .subject("Test")
                .templateName("emails/test")
                .variables(Map.of("key", "value"))
                .fromAddress("sender@test.com")
                .priority(EmailPriority.HIGH)
                .status(EmailStatus.QUEUED)
                .queuedAt(now)
                .build();

        assertThat(email.getId()).isEqualTo(id);
        assertThat(email.getToAddresses()).containsExactly("test@example.com");
        assertThat(email.getPriority()).isEqualTo(EmailPriority.HIGH);
        assertThat(email.getStatus()).isEqualTo(EmailStatus.QUEUED);
    }

    private Email createTestEmail() {
        return Email.builder()
                .id(UUID.randomUUID())
                .toAddresses(List.of("test@example.com"))
                .subject("Test Subject")
                .templateName("emails/test")
                .variables(Map.of("key", "value"))
                .fromAddress("sender@ecclesiaflow.com")
                .priority(EmailPriority.NORMAL)
                .status(EmailStatus.QUEUED)
                .queuedAt(LocalDateTime.now())
                .build();
    }
}
