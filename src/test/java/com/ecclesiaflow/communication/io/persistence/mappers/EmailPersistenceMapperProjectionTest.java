package com.ecclesiaflow.communication.io.persistence.mappers;

import com.ecclesiaflow.communication.business.domain.email.EmailPriority;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import com.ecclesiaflow.communication.io.persistence.jpa.EmailEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailPersistenceMapperProjectionTest {

    private static class StubMapper implements EmailPersistenceMapper {
        @Override
        public com.ecclesiaflow.communication.business.domain.email.Email toDomain(com.ecclesiaflow.communication.io.persistence.jpa.EmailEntity entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public com.ecclesiaflow.communication.io.persistence.jpa.EmailEntity toEntity(com.ecclesiaflow.communication.business.domain.email.Email email) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateEntityFromDomain(com.ecclesiaflow.communication.business.domain.email.Email email, @org.mapstruct.MappingTarget com.ecclesiaflow.communication.io.persistence.jpa.EmailEntity entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public com.ecclesiaflow.communication.business.domain.email.Email toDomain(com.ecclesiaflow.communication.io.persistence.projections.EmailSummaryProjection projection) {
            throw new UnsupportedOperationException();
        }
    }

    private final EmailPersistenceMapper mapper = new StubMapper();

    @Test
    void defaultConverters_shouldHandleEnumsAndLists() {
        assertThat(mapper.entityStatusToDomain(EmailEntity.EmailStatusEnum.QUEUED)).isEqualTo(EmailStatus.QUEUED);
        assertThat(mapper.entityPriorityToDomain(EmailEntity.EmailPriorityEnum.HIGH)).isEqualTo(EmailPriority.HIGH);

        assertThat(mapper.stringToList("a@b.com,c@d.com")).containsExactly("a@b.com","c@d.com");
        assertThat(mapper.listToString(java.util.List.of("x","y"))).isEqualTo("x,y");
    }
}
