package com.example.toeic_api.modules.vocabulary.model;

import jakarta.persistence.*;

import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyFamilyMember {
    @Column(name = "family_word", nullable = false, length = 100)
    private String word;

    @Column(length = 40)
    private String partOfSpeech;

    @Column(length = 500)
    private String meaning;
}
