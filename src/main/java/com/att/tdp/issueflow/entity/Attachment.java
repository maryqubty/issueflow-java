package com.att.tdp.issueflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Column(nullable = false)
    private String filename;

    private String contentType;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] data;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
