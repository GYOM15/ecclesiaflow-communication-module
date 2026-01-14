package com.ecclesiaflow.communication.web.mappers;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.email.EmailPriority;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import com.ecclesiaflow.communication.web.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * MapStruct mapper for Email domain ↔ REST DTO conversions.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmailDtoMapper {

    @Mapping(target = "emailId", source = "id", qualifiedByName = "uuidToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToSendEmailResponseEnum")
    @Mapping(target = "queuedAt", source = "queuedAt", qualifiedByName = "localDateTimeToOffsetDateTime")
    @Mapping(target = "message", constant = "Email processed successfully")
    SendEmailResponse toSendEmailResponse(Email email);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fromAddress", ignore = true) // Will be set by delegate with default config
    @Mapping(target = "toAddresses", source = "to")
    @Mapping(target = "variables", source = "variables")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "stringToPriority")
    @Mapping(target = "status", constant = "QUEUED")
    @Mapping(target = "queuedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "retryCount", constant = "0")
    Email toDomain(SendEmailRequest request);

    @Mapping(target = "emailId", source = "id", qualifiedByName = "uuidToString")
    @Mapping(target = "to", source = "toAddresses")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToEmailStatusResponseEnum")
    @Mapping(target = "queuedAt", source = "queuedAt", qualifiedByName = "localDateTimeToOffsetDateTime")
    @Mapping(target = "sentAt", source = "sentAt", qualifiedByName = "localDateTimeToOffsetDateTime")
    @Mapping(target = "deliveredAt", source = "deliveredAt", qualifiedByName = "localDateTimeToOffsetDateTime")
    @Mapping(target = "failedAt", source = "failedAt", qualifiedByName = "localDateTimeToOffsetDateTime")
    @Mapping(target = "openedAt", source = "deliveredAt", qualifiedByName = "localDateTimeToOffsetDateTime")
    @Mapping(target = "clicks", constant = "0")
    EmailStatusResponse toEmailStatusResponse(Email email);

    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("statusToSendEmailResponseEnum")
    default SendEmailResponse.StatusEnum statusToSendEmailResponseEnum(EmailStatus status) {
        if (status == null) return null;
        return SendEmailResponse.StatusEnum.valueOf(status.name());
    }

    @Named("statusToEmailStatusResponseEnum")
    default EmailStatusResponse.StatusEnum statusToEmailStatusResponseEnum(EmailStatus status) {
        if (status == null) return null;
        return EmailStatusResponse.StatusEnum.valueOf(status.name());
    }

    @Named("stringToPriority")
    default EmailPriority stringToPriority(SendEmailRequest.PriorityEnum priority) {
        if (priority == null) return EmailPriority.NORMAL;
        return EmailPriority.valueOf(priority.name());
    }

    @Named("localDateTimeToOffsetDateTime")
    default OffsetDateTime localDateTimeToOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atOffset(ZoneOffset.UTC) : null;
    }
}
