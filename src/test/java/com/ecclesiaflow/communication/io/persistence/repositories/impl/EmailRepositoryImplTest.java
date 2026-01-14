package com.ecclesiaflow.communication.io.persistence.repositories.impl;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.email.EmailPriority;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import com.ecclesiaflow.communication.io.persistence.jpa.EmailEntity;
import com.ecclesiaflow.communication.io.persistence.jpa.SpringDataEmailRepository;
import com.ecclesiaflow.communication.io.persistence.mappers.EmailPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailRepositoryImplTest {

    @Mock
    private SpringDataEmailRepository springRepo;

    @Mock
    private EmailPersistenceMapper mapper;

    private EmailRepositoryImpl newRepo() {
        return new EmailRepositoryImpl(springRepo, mapper);
    }

    @Test
    void save_shouldCreateWhenIdIsNull() {
        EmailRepositoryImpl repo = newRepo();
        Email domain = Email.builder()
                .toAddresses(List.of("a@b.com"))
                .fromAddress("noreply@ecclesiaflow.com")
                .subject("S")
                .templateName("t")
                .priority(EmailPriority.NORMAL)
                .status(EmailStatus.QUEUED)
                .build();
        EmailEntity entity = EmailEntity.builder().subject("S").build();
        EmailEntity savedEntity = EmailEntity.builder().subject("S").build();
        Email mappedBack = domain.toBuilder().id(UUID.randomUUID()).build();

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(springRepo.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(mappedBack);

        Email result = repo.save(domain);

        assertThat(result.getId()).isNotNull();
        verify(mapper).toEntity(domain);
        verify(springRepo).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    void save_shouldUpdateWhenIdPresent() {
        EmailRepositoryImpl repo = newRepo();
        UUID id = UUID.randomUUID();
        Email domain = Email.builder().id(id).subject("S").build();
        EmailEntity existing = EmailEntity.builder().build();
        EmailEntity saved = EmailEntity.builder().build();
        Email mappedBack = domain;

        when(springRepo.findById(id)).thenReturn(Optional.of(existing));
        when(springRepo.save(existing)).thenReturn(saved);
        when(mapper.toDomain(saved)).thenReturn(mappedBack);

        Email result = repo.save(domain);

        assertThat(result).isSameAs(mappedBack);
        verify(mapper).updateEntityFromDomain(domain, existing);
        verify(springRepo).save(existing);
    }

    @Test
    void findById_shouldMapWhenPresent() {
        EmailRepositoryImpl repo = newRepo();
        UUID id = UUID.randomUUID();
        EmailEntity entity = EmailEntity.builder().build();
        Email mapped = Email.builder().id(id).build();

        when(springRepo.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(mapped);

        Optional<Email> result = repo.findById(id);

        assertThat(result).contains(mapped);
    }

    @Test
    void findAll_shouldMapPage() {
        EmailRepositoryImpl repo = newRepo();
        EmailEntity entity = EmailEntity.builder().build();
        Page<EmailEntity> page = new PageImpl<>(List.of(entity));
        when(springRepo.findAll(any(Pageable.class))).thenReturn(page);
        when(mapper.toDomain(entity)).thenReturn(Email.builder().id(UUID.randomUUID()).build());

        Page<Email> result = repo.findAll(PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findByStatus_shouldMap() {
        EmailRepositoryImpl repo = newRepo();
        EmailEntity entity = EmailEntity.builder().build();
        Page<EmailEntity> page = new PageImpl<>(List.of(entity));
        when(springRepo.findByStatus(eq(EmailEntity.EmailStatusEnum.QUEUED), any(Pageable.class))).thenReturn(page);
        when(mapper.toDomain(entity)).thenReturn(Email.builder().id(UUID.randomUUID()).build());

        Page<Email> result = repo.findByStatus(EmailStatus.QUEUED, PageRequest.of(0, 5));
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findQueuedEmails_shouldUseLimitAndMap() {
        EmailRepositoryImpl repo = newRepo();
        EmailEntity e1 = EmailEntity.builder().build();
        when(springRepo.findQueuedEmailsForProcessing(eq(EmailEntity.EmailStatusEnum.QUEUED), any(Pageable.class)))
                .thenReturn(List.of(e1));
        when(mapper.toDomain(e1)).thenReturn(Email.builder().id(UUID.randomUUID()).build());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        List<Email> result = repo.findQueuedEmails(7);
        assertThat(result).hasSize(1);

        verify(springRepo).findQueuedEmailsForProcessing(eq(EmailEntity.EmailStatusEnum.QUEUED), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(7);
    }

    @Test
    void countByStatus_shouldDelegate() {
        EmailRepositoryImpl repo = newRepo();
        when(springRepo.countByStatus(EmailEntity.EmailStatusEnum.SENT)).thenReturn(42L);
        long count = repo.countByStatus(EmailStatus.SENT);
        assertThat(count).isEqualTo(42L);
    }

    @Test
    void delete_shouldMapAndDelete() {
        EmailRepositoryImpl repo = newRepo();
        Email domain = Email.builder().id(UUID.randomUUID()).build();
        EmailEntity entity = EmailEntity.builder().build();
        when(mapper.toEntity(domain)).thenReturn(entity);

        repo.delete(domain);

        verify(springRepo).delete(entity);
    }

    @Test
    void existsById_shouldDelegate() {
        EmailRepositoryImpl repo = newRepo();
        UUID id = UUID.randomUUID();
        when(springRepo.existsById(id)).thenReturn(true);
        assertThat(repo.existsById(id)).isTrue();
    }

    @Test
    void save_shouldThrowWhenIdPresentButNotFound() {
        EmailRepositoryImpl repo = newRepo();
        UUID id = UUID.randomUUID();
        Email domain = Email.builder().id(id).subject("S").build();

        when(springRepo.findById(id)).thenReturn(Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> repo.save(domain))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(id.toString());

        verify(mapper, never()).updateEntityFromDomain(any(), any());
        verify(springRepo, never()).save(any());
    }

    @Test
    void findByTemplateName_shouldMapPage() {
        EmailRepositoryImpl repo = newRepo();
        EmailEntity entity = EmailEntity.builder().build();
        Page<EmailEntity> page = new PageImpl<>(List.of(entity));
        when(springRepo.findByTemplateName(eq("emails/welcome"), any(Pageable.class))).thenReturn(page);
        when(mapper.toDomain(entity)).thenReturn(Email.builder().id(UUID.randomUUID()).build());

        Page<Email> result = repo.findByTemplateName("emails/welcome", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(springRepo).findByTemplateName(eq("emails/welcome"), any(Pageable.class));
    }

    @Test
    void findByPriority_shouldMapPage() {
        EmailRepositoryImpl repo = newRepo();
        EmailEntity entity = EmailEntity.builder().build();
        Page<EmailEntity> page = new PageImpl<>(List.of(entity));
        when(springRepo.findByPriority(eq(EmailEntity.EmailPriorityEnum.HIGH), any(Pageable.class))).thenReturn(page);
        when(mapper.toDomain(entity)).thenReturn(Email.builder().id(UUID.randomUUID()).build());

        Page<Email> result = repo.findByPriority(EmailPriority.HIGH, PageRequest.of(0, 5));

        assertThat(result.getContent()).hasSize(1);
        verify(springRepo).findByPriority(eq(EmailEntity.EmailPriorityEnum.HIGH), any(Pageable.class));
    }

    @Test
    void findFailedEmailsForRetry_shouldUseParamsAndMap() {
        EmailRepositoryImpl repo = newRepo();
        int limit = 7;
        EmailEntity e1 = EmailEntity.builder().build();
        when(springRepo.findFailedEmailsForRetry(eq(EmailEntity.EmailStatusEnum.FAILED), eq(3), any(Pageable.class)))
                .thenReturn(List.of(e1));
        when(mapper.toDomain(e1)).thenReturn(Email.builder().id(UUID.randomUUID()).build());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        var result = repo.findFailedEmailsForRetry(3, limit);

        assertThat(result).hasSize(1);
        verify(springRepo).findFailedEmailsForRetry(eq(EmailEntity.EmailStatusEnum.FAILED), eq(3), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(limit);
    }
}
