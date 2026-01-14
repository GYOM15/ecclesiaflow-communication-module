package com.ecclesiaflow.communication.application.messaging;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailMessageTest {

    @Test
    void builder_shouldSetAllFields() {
        UUID id = UUID.randomUUID();
        EmailMessage msg = EmailMessage.builder()
                .emailId(id)
                .from("from@ecclesiaflow.com")
                .to(List.of("a@b.com"))
                .subject("Subject")
                .templateName("emails/test")
                .variables(Map.of("k","v"))
                .priority("HIGH")
                .retryCount(2)
                .build();

        assertThat(msg.getEmailId()).isEqualTo(id);
        assertThat(msg.getFrom()).isEqualTo("from@ecclesiaflow.com");
        assertThat(msg.getTo()).containsExactly("a@b.com");
        assertThat(msg.getSubject()).isEqualTo("Subject");
        assertThat(msg.getTemplateName()).isEqualTo("emails/test");
        assertThat(msg.getVariables()).containsEntry("k","v");
        assertThat(msg.getPriority()).isEqualTo("HIGH");
        assertThat(msg.getRetryCount()).isEqualTo(2);
    }
}
