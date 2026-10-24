package com.example.toeic_api.modules.vocabulary.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VocabularyWordResponse {
    Long id;
    Long setId;
    String word;
    String meaning;
    String partOfSpeech;
    String exampleSentence;
    String exampleTranslation;
    String pronunciation;
    Set<String> synonyms;
    java.util.List<
                    com.example.toeic_api.modules.vocabulary.dto.request.VocabularyLearningContent
                            .Sense>
            senses;
    java.util.List<
                    com.example.toeic_api.modules.vocabulary.dto.request.VocabularyLearningContent
                            .FamilyMember>
            family;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
