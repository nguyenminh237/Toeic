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
@Table(name = "vocabulary_words")
public class VocabularyWord {
    @Builder.Default
    @Column(nullable = false)
    boolean archived = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "owner_id", updatable = false)
    String ownerId;

    @ManyToMany
    @JoinTable(
            name = "vocabulary_word_sets",
            joinColumns = @JoinColumn(name = "word_id"),
            inverseJoinColumns = @JoinColumn(name = "set_id"))
    @Builder.Default
    Set<VocabularySet> sets = new java.util.HashSet<>();

    String audioUrl;

    @Column(nullable = false)
    String word;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    String meaning;

    @Column(nullable = false)
    String partOfSpeech; // noun, verb, adjective, adverb, ...

    @Column(columnDefinition = "LONGTEXT")
    String exampleSentence;

    @Column(columnDefinition = "LONGTEXT")
    String exampleTranslation;

    String pronunciation; // IPA / phonetic transcription

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

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<VocabularySynonym> synonyms;

    @OneToMany(mappedBy = "word")
    Set<UserVocabularyItem> userItems;

    @OneToMany(mappedBy = "word")
    Set<UserVocabularyProgress> userProgresses;

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    java.util.List<VocabularySense> senses = new java.util.ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "vocabulary_word_family", joinColumns = @JoinColumn(name = "word_id"))
    @OrderColumn(name = "position")
    @Builder.Default
    java.util.List<VocabularyFamilyMember> family = new java.util.ArrayList<>();
}
