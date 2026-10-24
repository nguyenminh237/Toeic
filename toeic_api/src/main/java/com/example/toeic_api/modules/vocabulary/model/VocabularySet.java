package com.example.toeic_api.modules.vocabulary.model;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "vocabulary_sets")
public class VocabularySet {
    @Builder.Default
    @Column(nullable = false)
    boolean archived = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    VocabularyCategory category;

    @Column(nullable = false)
    String setName; // 'Contracts', 'Marketing', 'Computers', ...

    @Column(columnDefinition = "LONGTEXT")
    String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(java.time.Clock.systemUTC());
        updatedAt = LocalDateTime.now(java.time.Clock.systemUTC());
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(java.time.Clock.systemUTC());
    }

    @ManyToMany(mappedBy = "sets")
    Set<VocabularyWord> words;
}
