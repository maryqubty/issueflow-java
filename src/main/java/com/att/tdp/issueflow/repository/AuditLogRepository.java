package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.AuditLog;
import com.att.tdp.issueflow.enums.Actor;
import com.att.tdp.issueflow.enums.AuditAction;
import com.att.tdp.issueflow.enums.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:entityType IS NULL OR a.entityType = :entityType) AND " +
            "(:entityId IS NULL OR a.entityId = :entityId) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:actor IS NULL OR a.actor = :actor)")
    List<AuditLog> findFiltered(
            @Param("entityType") EntityType entityType,
            @Param("entityId") Long entityId,
            @Param("action") AuditAction action,
            @Param("actor") Actor actor
    );
}
