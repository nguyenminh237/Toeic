package com.example.toeic_api.modules.vocabulary.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

public final class VocabularyLearningContent {
    private VocabularyLearningContent() {}

    public record Sense(
            @NotBlank @Size(max = 2000) String meaning,
            @NotBlank @Size(max = 40) String partOfSpeech,
            @Min(10) @Max(990) Integer minimumTargetScore,
            @Size(max = 1000) String exampleSentence,
            @Size(max = 1000) String exampleTranslation,
            @Size(max = 1000) String studyNote,
            @Size(max = 8) List<@NotBlank @Size(max = 300) String> synonyms,
            @Size(max = 8) List<@NotBlank @Size(max = 300) String> antonyms,
            @Size(max = 8) List<@NotBlank @Size(max = 300) String> collocations) {}

    public record FamilyMember(
            @NotBlank @Size(max = 100) String word,
            @Size(max = 40) String partOfSpeech,
            @Size(max = 500) String meaning) {}
}
