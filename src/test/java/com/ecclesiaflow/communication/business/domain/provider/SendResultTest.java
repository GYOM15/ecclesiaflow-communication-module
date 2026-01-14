package com.ecclesiaflow.communication.business.domain.provider;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SendResultTest {

    @Test
    void success_shouldPopulateFields() {
        SendResult result = SendResult.success("msg-123", "gmail");

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessageId()).isEqualTo("msg-123");
        assertThat(result.getProviderName()).isEqualTo("gmail");
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    void failure_shouldPopulateFields() {
        SendResult result = SendResult.failure("timeout", "sendgrid");

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("timeout");
        assertThat(result.getProviderName()).isEqualTo("sendgrid");
        assertThat(result.getMessageId()).isNull();
    }
}
