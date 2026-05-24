package com.att.tdp.issueflow.entity;

import com.att.tdp.issueflow.enums.Actor;
import com.att.tdp.issueflow.enums.AuditAction;
import com.att.tdp.issueflow.enums.EntityType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityType entityType;

    @Column(nullable = false)
    private Long entityId;

    private Long performedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Actor actor;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
