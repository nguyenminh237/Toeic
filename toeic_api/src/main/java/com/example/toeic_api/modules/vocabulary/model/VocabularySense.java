package com.example.toeic_api.modules.vocabulary.model;

import jakarta.persistence.*;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vocabulary_senses")
@Getter
@Setter
@NoArgsConstructor
public class VocabularySense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private VocabularyWord word;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String meaning;

    @Column(nullable = false)
    private String partOfSpeech;

    private Integer minimumTargetScore;

    @Column(columnDefinition = "LONGTEXT")
    private String exampleSentence;

    @Column(columnDefinition = "LONGTEXT")
    private String exampleTranslation;

    @Column(columnDefinition = "LONGTEXT")
    private String studyNote;

    @ElementCollection
    @CollectionTable(
            name = "vocabulary_sense_phrases",
            joinColumns = @JoinColumn(name = "sense_id"))
    @OrderColumn(name = "position")
    private List<Phrase> phrases = new ArrayList<>();

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Phrase {
        @Column(nullable = false, length = 20)
        private String kind;

        @Column(nullable = false, length = 300)
        private String phrase;
    }
}
