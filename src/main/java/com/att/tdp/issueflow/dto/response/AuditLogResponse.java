package com.att.tdp.issueflow.dto.response;

import com.att.tdp.issueflow.enums.Actor;
import com.att.tdp.issueflow.enums.AuditAction;
import com.att.tdp.issueflow.enums.EntityType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private AuditAction action;
    private EntityType entityType;
    private Long entityId;
    private Long performedBy;
    private Actor actor;
    private LocalDateTime timestamp;
}
