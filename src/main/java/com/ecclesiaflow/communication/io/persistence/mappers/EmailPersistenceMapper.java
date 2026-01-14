package com.ecclesiaflow.communication.io.persistence.mappers;

import com.ecclesiaflow.communication.business.domain.email.Email;
import com.ecclesiaflow.communication.business.domain.email.EmailPriority;
import com.ecclesiaflow.communication.business.domain.email.EmailStatus;
import com.ecclesiaflow.communication.io.persistence.jpa.EmailEntity;
import com.ecclesiaflow.communication.io.persistence.projections.EmailSummaryProjection;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * MapStruct mapper for EmailEntity and Email domain model.
 *
 * @author EcclesiaFlow Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmailPersistenceMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "toAddresses", source = "toAddresses", qualifiedByName = "stringToList")
    @Mapping(target = "variables", source = "variables", qualifiedByName = "stringToMap")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "entityPriorityToDomain")
    @Mapping(target = "status", source = "status", qualifiedByName = "entityStatusToDomain")
    Email toDomain(EmailEntity entity);

    @Mapping(target = "toAddresses", source = "toAddresses", qualifiedByName = "listToString")
    @Mapping(target = "variables", source = "variables", qualifiedByName = "mapToString")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "domainPriorityToEntity")
    @Mapping(target = "status", source = "status", qualifiedByName = "domainStatusToEntity")
    EmailEntity toEntity(Email email);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "toAddresses", source = "toAddresses", qualifiedByName = "listToString")
    @Mapping(target = "variables", source = "variables", qualifiedByName = "mapToString")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "domainPriorityToEntity")
    @Mapping(target = "status", source = "status", qualifiedByName = "domainStatusToEntity")
    void updateEntityFromDomain(Email email, @MappingTarget EmailEntity entity);

    @Mapping(target = "toAddresses", source = "toAddresses", qualifiedByName = "stringToList")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "entityPriorityToDomain")
    @Mapping(target = "status", source = "status", qualifiedByName = "entityStatusToDomain")
    Email toDomain(EmailSummaryProjection projection);

    @Named("stringToList")
    default List<String> stringToList(String value) {
        if (value == null || value.isEmpty()) return List.of();
        return Arrays.asList(value.split(","));
    }

    @Named("listToString")
    default String listToString(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join(",", list);
    }

    @Named("stringToMap")
    default Map<String, Object> stringToMap(String value) {
        if (value == null || value.isEmpty()) return Map.of();
        try {
            return OBJECT_MAPPER.readValue(value, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    @Named("mapToString")
    default String mapToString(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return "{}";
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @Named("entityPriorityToDomain")
    default EmailPriority entityPriorityToDomain(EmailEntity.EmailPriorityEnum priority) {
        if (priority == null) return null;
        return EmailPriority.valueOf(priority.name());
    }

    @Named("domainPriorityToEntity")
    default EmailEntity.EmailPriorityEnum domainPriorityToEntity(EmailPriority priority) {
        if (priority == null) return null;
        return EmailEntity.EmailPriorityEnum.valueOf(priority.name());
    }

    @Named("entityStatusToDomain")
    default EmailStatus entityStatusToDomain(EmailEntity.EmailStatusEnum status) {
        if (status == null) return null;
        return EmailStatus.valueOf(status.name());
    }

    @Named("domainStatusToEntity")
    default EmailEntity.EmailStatusEnum domainStatusToEntity(EmailStatus status) {
        if (status == null) return null;
        return EmailEntity.EmailStatusEnum.valueOf(status.name());
    }
}
