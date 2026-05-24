package com.att.tdp.issueflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "revoked_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(nullable = false)
    private LocalDateTime revokedAt;
}
