package com.example.toeic_api.modules.vocabulary.model;

import com.example.toeic_api.modules.users.model.User;

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
@Table(
        name = "vocabulary_categories",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_vocab_category_owner_name",
                        columnNames = {"created_by", "category_name"}))
public class VocabularyCategory {
    @Builder.Default
    @Column(nullable = false)
    boolean archived = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String categoryName; // '600 từ Toeic', '1000 từ Part 7', ...

    @Column(columnDefinition = "LONGTEXT")
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    User createdBy;

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

    @OneToMany(mappedBy = "category")
    Set<VocabularySet> sets;
}
