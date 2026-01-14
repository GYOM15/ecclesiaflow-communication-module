package com.ecclesiaflow.communication.web.mappers;

import com.ecclesiaflow.communication.business.domain.email.EmailPriority;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EmailDtoMapperTest {

    private static class StubMapper implements EmailDtoMapper {
        @Override
        public com.ecclesiaflow.communication.web.model.SendEmailResponse toSendEmailResponse(com.ecclesiaflow.communication.business.domain.email.Email email) {
            throw new UnsupportedOperationException();
        }

        @Override
        public com.ecclesiaflow.communication.business.domain.email.Email toDomain(com.ecclesiaflow.communication.web.model.SendEmailRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public com.ecclesiaflow.communication.web.model.EmailStatusResponse toEmailStatusResponse(com.ecclesiaflow.communication.business.domain.email.Email email) {
            throw new UnsupportedOperationException();
        }
    }

    private final EmailDtoMapper mapper = new StubMapper();

    @Test
    void uuidToString_shouldHandleNullAndValue() {
        assertThat(mapper.uuidToString(null)).isNull();
        java.util.UUID id = java.util.UUID.randomUUID();
        assertThat(mapper.uuidToString(id)).isEqualTo(id.toString());
    }

    @Test
    void statusConverters_shouldMapEnumsSafely() {
        assertThat(mapper.statusToSendEmailResponseEnum(null)).isNull();
        assertThat(mapper.statusToSendEmailResponseEnum(EmailStatus.QUEUED).name()).isEqualTo("QUEUED");

        assertThat(mapper.statusToEmailStatusResponseEnum(null)).isNull();
        assertThat(mapper.statusToEmailStatusResponseEnum(EmailStatus.SENT).name()).isEqualTo("SENT");
    }

    @Test
    void stringToPriority_shouldDefaultToNormalWhenNull() {
        assertThat(mapper.stringToPriority(null)).isEqualTo(EmailPriority.NORMAL);
        // Simulate mapping enum name through same value
        assertThat(mapper.stringToPriority(
                com.ecclesiaflow.communication.web.model.SendEmailRequest.PriorityEnum.HIGH)
        ).isEqualTo(EmailPriority.HIGH);
    }

    @Test
    void localDateTimeToOffsetDateTime_shouldHandleNullAndValue() {
        assertThat(mapper.localDateTimeToOffsetDateTime(null)).isNull();
        LocalDateTime now = LocalDateTime.now();
        OffsetDateTime odt = mapper.localDateTimeToOffsetDateTime(now);
        assertThat(odt).isNotNull();
        assertThat(odt.getOffset()).isEqualTo(java.time.ZoneOffset.UTC);
        assertThat(odt.toLocalDateTime()).isEqualTo(now);
    }
}
