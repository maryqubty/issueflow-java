package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.response.AuditLogResponse;
import com.att.tdp.issueflow.entity.AuditLog;
import com.att.tdp.issueflow.enums.Actor;
import com.att.tdp.issueflow.enums.AuditAction;
import com.att.tdp.issueflow.enums.EntityType;
import com.att.tdp.issueflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void record(AuditAction action, EntityType entityType, Long entityId, Long performedBy, Actor actor) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .performedBy(performedBy)
                .actor(actor)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLogResponse> getFiltered(String entityTypeStr, Long entityId, String actionStr, String actorStr) {
        EntityType entityType = entityTypeStr != null ? EntityType.valueOf(entityTypeStr.toUpperCase()) : null;
        AuditAction action = actionStr != null ? AuditAction.valueOf(actionStr.toUpperCase()) : null;
        Actor actor = actorStr != null ? Actor.valueOf(actorStr.toUpperCase()) : null;

        return auditLogRepository.findFiltered(entityType, entityId, action, actor)
                .stream().map(this::toResponse).toList();
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .performedBy(log.getPerformedBy())
                .actor(log.getActor())
                .timestamp(log.getTimestamp())
                .build();
    }
}
