package com.example.toeic_api.modules.vocabulary.model;

import com.example.toeic_api.modules.users.model.User;

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
        name = "user_vocabulary_progress",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_vocab_progress",
                        columnNames = {"user_id", "word_id"}))
public class UserVocabularyProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    VocabularyWord word;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(nullable = false)
    VocabularyProgressStatus status; // learning, reviewing, mastered

    @Column(name = "last_reviewed")
    LocalDateTime lastReviewed;

    @Column(nullable = false)
    @Builder.Default
    Integer reviewCount = 0;
}
