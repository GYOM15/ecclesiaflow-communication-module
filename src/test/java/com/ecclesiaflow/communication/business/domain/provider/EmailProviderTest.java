package com.ecclesiaflow.communication.business.domain.provider;

import com.ecclesiaflow.communication.business.domain.email.Email;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailProviderTest {

    @Test
    void defaultPriority_shouldBe100() {
        EmailProvider provider = new EmailProvider() {
            @Override
            public SendResult send(Email email) {
                return SendResult.success("msg", "provider");
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public String getProviderName() {
                return "provider";
            }
        };

        assertThat(provider.getPriority()).isEqualTo(100);
    }
}
