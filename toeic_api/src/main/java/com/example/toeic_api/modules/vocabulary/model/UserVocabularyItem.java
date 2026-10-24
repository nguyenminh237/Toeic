package com.example.toeic_api.modules.vocabulary.model;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "user_vocabulary_items",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_collection_word",
                        columnNames = {"collection_id", "word_id"}))
public class UserVocabularyItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    UserVocabularyCollection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    VocabularyWord word;

    @Column(name = "added_at", nullable = false, updatable = false)
    LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now(java.time.Clock.systemUTC());
    }
}
