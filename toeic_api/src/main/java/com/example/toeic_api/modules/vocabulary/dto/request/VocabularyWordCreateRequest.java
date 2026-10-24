package com.example.toeic_api.modules.vocabulary.dto.request;

import com.example.toeic_api.modules.vocabulary.dto.request.VocabularyLearningContent.FamilyMember;
import com.example.toeic_api.modules.vocabulary.dto.request.VocabularyLearningContent.Sense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VocabularyWordCreateRequest {
    @NotNull(message = "SET_ID_REQUIRED")
    Long setId;

    @NotBlank(message = "WORD_REQUIRED")
    String word;

    @NotBlank(message = "MEANING_REQUIRED")
    String meaning;

    @NotBlank(message = "PART_OF_SPEECH_REQUIRED")
    String partOfSpeech;

    String exampleSentence;
    String exampleTranslation;
    String pronunciation;

    Set<String> synonyms; // "abide by the law: tuân thủ luật pháp"

    @jakarta.validation.Valid
    @jakarta.validation.constraints.Size(max = 5)
    java.util.List<@jakarta.validation.constraints.NotNull Sense> senses;

    @jakarta.validation.Valid
    @jakarta.validation.constraints.Size(max = 12)
    java.util.List<@jakarta.validation.constraints.NotNull FamilyMember> family;
}
