package com.ecclesiaflow.communication.application.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailConfigurationPropertiesTest {

    @Test
    void defaults_shouldBeApplied() {
        EmailConfigurationProperties props = new EmailConfigurationProperties();

        assertThat(props.getFrom()).isNull();
        assertThat(props.getFromName()).isEqualTo("EcclesiaFlow");
    }

    @Test
    void setters_shouldUpdateValues() {
        EmailConfigurationProperties props = new EmailConfigurationProperties();

        props.setFrom("noreply@ecclesiaflow.com");
        props.setFromName("EcclesiaFlow Team");

        assertThat(props.getFrom()).isEqualTo("noreply@ecclesiaflow.com");
        assertThat(props.getFromName()).isEqualTo("EcclesiaFlow Team");
    }
}
