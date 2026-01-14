package com.ecclesiaflow.communication.io.persistence.mappers;

import com.ecclesiaflow.communication.business.domain.email.EmailPriority;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import com.ecclesiaflow.communication.io.persistence.jpa.EmailEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EmailPersistenceMapperTest {

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
    void stringListConversions_shouldBeInverseAndHandleNulls() {
        assertThat(mapper.stringToList(null)).isEmpty();
        assertThat(mapper.stringToList("")).isEmpty();
        assertThat(mapper.stringToList("a,b,c")).containsExactly("a","b","c");

        assertThat(mapper.listToString(null)).isEqualTo("");
        assertThat(mapper.listToString(List.of())).isEqualTo("");
        assertThat(mapper.listToString(List.of("a","b"))).isEqualTo("a,b");
    }

    @Test
    void mapStringConversions_shouldBeInverseAndHandleNulls() {
        assertThat(mapper.stringToMap(null)).isEmpty();
        assertThat(mapper.stringToMap("")).isEmpty();
        assertThat(mapper.mapToString(null)).isEqualTo("{}");
        assertThat(mapper.mapToString(Map.of())).isEqualTo("{}");

        Map<String,Object> map = Map.of("k","v", "n", 1);
        String json = mapper.mapToString(map);
        assertThat(json).contains("\"k\"", "\"n\"");
        Map<String,Object> roundtrip = mapper.stringToMap(json);
        assertThat(roundtrip).containsKeys("k","n");
    }

    @Test
    void stringToMap_shouldReturnEmptyMapOnInvalidJson() {
        Map<String, Object> result = mapper.stringToMap("not valid json {{{");
        assertThat(result).isEmpty();
    }

    @Test
    void mapToString_shouldReturnEmptyJsonOnSerializationError() {
        Map<String, Object> unserializable = new java.util.HashMap<>();
        unserializable.put("bad", new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("Cannot serialize");
            }
        });
        String result = mapper.mapToString(unserializable);
        assertThat(result).isEqualTo("{}");
    }

    @Test
    void enumConversions_shouldMapBothWaysAndHandleNulls() {
        assertThat(mapper.entityPriorityToDomain(null)).isNull();
        assertThat(mapper.domainPriorityToEntity(null)).isNull();
        assertThat(mapper.entityStatusToDomain(null)).isNull();
        assertThat(mapper.domainStatusToEntity(null)).isNull();

        assertThat(mapper.entityPriorityToDomain(EmailEntity.EmailPriorityEnum.HIGH)).isEqualTo(EmailPriority.HIGH);
        assertThat(mapper.domainPriorityToEntity(EmailPriority.LOW)).isEqualTo(EmailEntity.EmailPriorityEnum.LOW);
        assertThat(mapper.entityStatusToDomain(EmailEntity.EmailStatusEnum.SENT)).isEqualTo(EmailStatus.SENT);
        assertThat(mapper.domainStatusToEntity(EmailStatus.FAILED)).isEqualTo(EmailEntity.EmailStatusEnum.FAILED);
    }
}
