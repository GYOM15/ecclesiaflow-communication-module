package com.ecclesiaflow.communication.business.exceptions;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNotFoundExceptionTest {

    @Test
    void constructorWithEmailId_shouldExposeIdAndMessage() {
        UUID id = UUID.randomUUID();

        EmailNotFoundException ex = new EmailNotFoundException(id);

        assertThat(ex).isNotNull();
        assertThat(ex.getEmailId()).isEqualTo(id);
        assertThat(ex.getMessage()).contains(id.toString());
    }

    @Test
    void constructorWithMessage_shouldExposeMessageAndNullId() {
        EmailNotFoundException ex = new EmailNotFoundException("custom");

        assertThat(ex).isNotNull();
        assertThat(ex.getEmailId()).isNull();
        assertThat(ex.getMessage()).isEqualTo("custom");
    }
}
