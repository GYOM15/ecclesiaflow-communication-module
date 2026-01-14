package com.ecclesiaflow.communication.business.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailSendingExceptionTest {

    @Test
    void constructorWithMessage_shouldExposeMessage() {
        EmailSendingException ex = new EmailSendingException("boom");

        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).isEqualTo("boom");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void constructorWithMessageAndCause_shouldExposeBoth() {
        RuntimeException cause = new RuntimeException("root");
        EmailSendingException ex = new EmailSendingException("boom", cause);

        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).isEqualTo("boom");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
