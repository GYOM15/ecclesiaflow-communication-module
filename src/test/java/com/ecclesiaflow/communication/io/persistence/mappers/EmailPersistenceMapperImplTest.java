package com.ecclesiaflow.communication.io.persistence.mappers;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.email.EmailPriority;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import com.ecclesiaflow.communication.io.persistence.jpa.EmailEntity;
import com.ecclesiaflow.communication.io.persistence.projections.EmailSummaryProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailPersistenceMapperImplTest {

    private EmailPersistenceMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new EmailPersistenceMapperImpl();
    }

    @Test
    void toDomain_fromEntity_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        EmailEntity entity = EmailEntity.builder()
                .fromAddress("from@test.com")
                .toAddresses("a@b.com,c@d.com")
                .subject("Subject")
                .templateName("emails/welcome")
                .variables("{\"key\":\"value\"}")
                .priority(EmailEntity.EmailPriorityEnum.HIGH)
                .status(EmailEntity.EmailStatusEnum.SENT)
                .provider("gmail")
                .messageId("msg-123")
                .queuedAt(now)
                .sentAt(now.plusMinutes(1))
                .deliveredAt(now.plusMinutes(2))
                .failedAt(null)
                .errorMessage(null)
                .retryCount(0)
                .build();
        entity.setId(id);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        Email domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getFromAddress()).isEqualTo("from@test.com");
        assertThat(domain.getToAddresses()).containsExactly("a@b.com", "c@d.com");
        assertThat(domain.getSubject()).isEqualTo("Subject");
        assertThat(domain.getTemplateName()).isEqualTo("emails/welcome");
        assertThat(domain.getVariables()).containsEntry("key", "value");
        assertThat(domain.getPriority()).isEqualTo(EmailPriority.HIGH);
        assertThat(domain.getStatus()).isEqualTo(EmailStatus.SENT);
        assertThat(domain.getProvider()).isEqualTo("gmail");
        assertThat(domain.getMessageId()).isEqualTo("msg-123");
        assertThat(domain.getQueuedAt()).isEqualTo(now);
        assertThat(domain.getSentAt()).isEqualTo(now.plusMinutes(1));
        assertThat(domain.getDeliveredAt()).isEqualTo(now.plusMinutes(2));
        assertThat(domain.getRetryCount()).isZero();
    }

    @Test
    void toDomain_fromEntity_shouldReturnNullForNullInput() {
        assertThat(mapper.toDomain((EmailEntity) null)).isNull();
    }

    @Test
    void toEntity_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Email domain = Email.builder()
                .id(id)
                .fromAddress("from@test.com")
                .toAddresses(List.of("a@b.com", "c@d.com"))
                .subject("Subject")
                .templateName("emails/welcome")
                .variables(Map.of("key", "value"))
                .priority(EmailPriority.HIGH)
                .status(EmailStatus.QUEUED)
                .provider("sendgrid")
                .messageId("msg-456")
                .queuedAt(now)
                .sentAt(now.plusMinutes(1))
                .deliveredAt(now.plusMinutes(2))
                .failedAt(now.plusMinutes(3))
                .errorMessage("error")
                .retryCount(2)
                .build();

        EmailEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getFromAddress()).isEqualTo("from@test.com");
        assertThat(entity.getToAddresses()).isEqualTo("a@b.com,c@d.com");
        assertThat(entity.getSubject()).isEqualTo("Subject");
        assertThat(entity.getTemplateName()).isEqualTo("emails/welcome");
        assertThat(entity.getVariables()).contains("\"key\"");
        assertThat(entity.getPriority()).isEqualTo(EmailEntity.EmailPriorityEnum.HIGH);
        assertThat(entity.getStatus()).isEqualTo(EmailEntity.EmailStatusEnum.QUEUED);
        assertThat(entity.getProvider()).isEqualTo("sendgrid");
        assertThat(entity.getMessageId()).isEqualTo("msg-456");
        assertThat(entity.getQueuedAt()).isEqualTo(now);
        assertThat(entity.getSentAt()).isEqualTo(now.plusMinutes(1));
        assertThat(entity.getDeliveredAt()).isEqualTo(now.plusMinutes(2));
        assertThat(entity.getFailedAt()).isEqualTo(now.plusMinutes(3));
        assertThat(entity.getErrorMessage()).isEqualTo("error");
        assertThat(entity.getRetryCount()).isEqualTo(2);
    }

    @Test
    void toEntity_shouldReturnNullForNullInput() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void updateEntityFromDomain_shouldUpdateFields() {
        EmailEntity entity = EmailEntity.builder()
                .fromAddress("old@test.com")
                .toAddresses("old@b.com")
                .subject("Old Subject")
                .status(EmailEntity.EmailStatusEnum.QUEUED)
                .build();

        Email domain = Email.builder()
                .fromAddress("new@test.com")
                .toAddresses(List.of("new@b.com"))
                .subject("New Subject")
                .status(EmailStatus.SENT)
                .priority(EmailPriority.LOW)
                .build();

        mapper.updateEntityFromDomain(domain, entity);

        assertThat(entity.getFromAddress()).isEqualTo("new@test.com");
        assertThat(entity.getToAddresses()).isEqualTo("new@b.com");
        assertThat(entity.getSubject()).isEqualTo("New Subject");
        assertThat(entity.getStatus()).isEqualTo(EmailEntity.EmailStatusEnum.SENT);
        assertThat(entity.getPriority()).isEqualTo(EmailEntity.EmailPriorityEnum.LOW);
    }

    @Test
    void updateEntityFromDomain_shouldDoNothingForNullDomain() {
        EmailEntity entity = EmailEntity.builder()
                .fromAddress("original@test.com")
                .build();

        mapper.updateEntityFromDomain(null, entity);

        assertThat(entity.getFromAddress()).isEqualTo("original@test.com");
    }

    @Test
    void toDomain_fromProjection_shouldMapFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        EmailSummaryProjection projection = mock(EmailSummaryProjection.class);
        when(projection.getId()).thenReturn(id);
        when(projection.getToAddresses()).thenReturn("a@b.com,c@d.com");
        when(projection.getSubject()).thenReturn("Subject");
        when(projection.getStatus()).thenReturn(EmailEntity.EmailStatusEnum.DELIVERED);
        when(projection.getPriority()).thenReturn(EmailEntity.EmailPriorityEnum.NORMAL);
        when(projection.getProvider()).thenReturn("gmail");
        when(projection.getQueuedAt()).thenReturn(now);
        when(projection.getSentAt()).thenReturn(now.plusMinutes(1));
        when(projection.getDeliveredAt()).thenReturn(now.plusMinutes(2));
        when(projection.getRetryCount()).thenReturn(1);

        Email domain = mapper.toDomain(projection);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getToAddresses()).containsExactly("a@b.com", "c@d.com");
        assertThat(domain.getSubject()).isEqualTo("Subject");
        assertThat(domain.getStatus()).isEqualTo(EmailStatus.DELIVERED);
        assertThat(domain.getPriority()).isEqualTo(EmailPriority.NORMAL);
        assertThat(domain.getProvider()).isEqualTo("gmail");
        assertThat(domain.getQueuedAt()).isEqualTo(now);
        assertThat(domain.getSentAt()).isEqualTo(now.plusMinutes(1));
        assertThat(domain.getDeliveredAt()).isEqualTo(now.plusMinutes(2));
        assertThat(domain.getRetryCount()).isEqualTo(1);
    }

    @Test
    void toDomain_fromProjection_shouldReturnNullForNullInput() {
        assertThat(mapper.toDomain((EmailSummaryProjection) null)).isNull();
    }
}
