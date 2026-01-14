package com.ecclesiaflow.communication.io.persistence.repositories.impl;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.email.EmailPriority;
import com.ecclesiaflow.communication.business.domain.email.EmailRepository;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import com.ecclesiaflow.communication.io.persistence.jpa.EmailEntity;
import com.ecclesiaflow.communication.io.persistence.jpa.SpringDataEmailRepository;
import com.ecclesiaflow.communication.io.persistence.mappers.EmailPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter for {@link EmailRepository}.
 * Bridges domain objects with Spring Data JPA persistence.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class EmailRepositoryImpl implements EmailRepository {

    private final SpringDataEmailRepository springDataRepository;
    private final EmailPersistenceMapper mapper;

    @Override
    public Email save(Email email) {
        EmailEntity entity;
        
        if (email.getId() != null) {
            entity = springDataRepository.findById(email.getId())
                    .orElseThrow(() -> new IllegalStateException("Email not found: " + email.getId()));
            mapper.updateEntityFromDomain(email, entity);
        } else {
            entity = mapper.toEntity(email);
        }
        
        EmailEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Email> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Email> findAll(Pageable pageable) {
        return springDataRepository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Email> findByStatus(EmailStatus status, Pageable pageable) {
        EmailEntity.EmailStatusEnum entityStatus = EmailEntity.EmailStatusEnum.valueOf(status.name());
        return springDataRepository.findByStatus(entityStatus, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Email> findByPriority(EmailPriority priority, Pageable pageable) {
        EmailEntity.EmailPriorityEnum entityPriority = EmailEntity.EmailPriorityEnum.valueOf(priority.name());
        return springDataRepository.findByPriority(entityPriority, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Email> findByTemplateName(String templateName, Pageable pageable) {
        return springDataRepository.findByTemplateName(templateName, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<Email> findQueuedEmails(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return springDataRepository.findQueuedEmailsForProcessing(
                EmailEntity.EmailStatusEnum.QUEUED,
                pageable
        ).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Email> findFailedEmailsForRetry(int maxRetryCount, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return springDataRepository.findFailedEmailsForRetry(
                EmailEntity.EmailStatusEnum.FAILED,
                maxRetryCount,
                pageable
        ).stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByStatus(EmailStatus status) {
        EmailEntity.EmailStatusEnum entityStatus = EmailEntity.EmailStatusEnum.valueOf(status.name());
        return springDataRepository.countByStatus(entityStatus);
    }

    @Override
    public void delete(Email email) {
        EmailEntity entity = mapper.toEntity(email);
        springDataRepository.delete(entity);
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataRepository.existsById(id);
    }
}
