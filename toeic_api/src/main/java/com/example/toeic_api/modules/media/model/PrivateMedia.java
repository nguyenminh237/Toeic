package com.example.toeic_api.modules.media.model;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "private_media")
@Getter
@Setter
public class PrivateMedia {
    @Id
    @Column(length = 36)
    private String id;

    private String ownerId;

    @Column(length = 80)
    private String storageKey;

    private String originalName;

    @Column(length = 100)
    private String contentType;

    private long sizeBytes;
    private LocalDateTime createdAt;
}
