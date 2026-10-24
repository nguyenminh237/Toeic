package com.example.toeic_api.shared.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class CatalogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Version protected long version;

    @Column(nullable = false)
    protected boolean archived;

    @Column(nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @Column(nullable = false)
    protected LocalDateTime updatedAt;

    @PrePersist
    void create() {
        createdAt = updatedAt = LocalDateTime.now(java.time.Clock.systemUTC());
    }

    @PreUpdate
    void update() {
        updatedAt = LocalDateTime.now(java.time.Clock.systemUTC());
    }
}
