package com.example.toeic_api.modules.vocabulary.service;

import com.example.toeic_api.modules.vocabulary.dto.request.*;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularyWordResponse;
import com.example.toeic_api.modules.vocabulary.model.*;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class VocabularyContentMapper {
    public void apply(VocabularyWord word, VocabularyWordCreateRequest request) {
        var source = request.getSenses();
        boolean legacyRequest = source == null;
        if (source == null || source.isEmpty()) {
            source =
                    new ArrayList<>(
                            List.of(
                                    new VocabularyLearningContent.Sense(
                                            request.getMeaning(),
                                            request.getPartOfSpeech(),
                                            null,
                                            request.getExampleSentence(),
                                            request.getExampleTranslation(),
                                            null,
                                            request.getSynonyms() == null
                                                    ? List.of()
                                                    : new ArrayList<>(request.getSynonyms()),
                                            List.of(),
                                            List.of())));
            if (legacyRequest) {
                for (var existing : word.getSenses().stream().skip(1).toList())
                    source.add(
                            new VocabularyLearningContent.Sense(
                                    existing.getMeaning(),
                                    existing.getPartOfSpeech(),
                                    existing.getMinimumTargetScore(),
                                    existing.getExampleSentence(),
                                    existing.getExampleTranslation(),
                                    existing.getStudyNote(),
                                    phrases(existing, "SYNONYM"),
                                    phrases(existing, "ANTONYM"),
                                    phrases(existing, "COLLOCATION")));
            }
        }
        var first = source.getFirst();
        word.setMeaning(first.meaning());
        word.setPartOfSpeech(first.partOfSpeech());
        word.setExampleSentence(first.exampleSentence());
        word.setExampleTranslation(first.exampleTranslation());
        if (word.getSynonyms() == null) word.setSynonyms(new HashSet<>());
        word.getSynonyms().clear();
        clean(first.synonyms())
                .forEach(
                        p ->
                                word.getSynonyms()
                                        .add(
                                                VocabularySynonym.builder()
                                                        .word(word)
                                                        .synonymPhrase(p)
                                                        .build()));
        for (int i = 0; i < source.size(); i++) {
            var input = source.get(i);
            VocabularySense sense;
            if (i < word.getSenses().size()) sense = word.getSenses().get(i);
            else {
                sense = new VocabularySense();
                sense.setWord(word);
                word.getSenses().add(sense);
            }
            sense.setPosition(i + 1);
            sense.setMeaning(input.meaning().trim());
            sense.setPartOfSpeech(input.partOfSpeech());
            sense.setMinimumTargetScore(input.minimumTargetScore());
            sense.setExampleSentence(input.exampleSentence());
            sense.setExampleTranslation(input.exampleTranslation());
            sense.setStudyNote(input.studyNote());
            sense.getPhrases().clear();
            phrases(sense, "SYNONYM", input.synonyms());
            phrases(sense, "ANTONYM", input.antonyms());
            phrases(sense, "COLLOCATION", input.collocations());
        }
        while (word.getSenses().size() > source.size()) word.getSenses().removeLast();
        if (request.getFamily() != null) {
            word.getFamily().clear();
            request.getFamily()
                    .forEach(
                            f ->
                                    word.getFamily()
                                            .add(
                                                    new VocabularyFamilyMember(
                                                            f.word(),
                                                            f.partOfSpeech(),
                                                            f.meaning())));
        }
    }

    public void enrich(VocabularyWord word, VocabularyWordResponse response) {
        response.setSenses(
                word.getSenses().stream()
                        .map(
                                s ->
                                        new VocabularyLearningContent.Sense(
                                                s.getMeaning(),
                                                s.getPartOfSpeech(),
                                                s.getMinimumTargetScore(),
                                                s.getExampleSentence(),
                                                s.getExampleTranslation(),
                                                s.getStudyNote(),
                                                s.getPhrases().stream()
                                                                        .noneMatch(
                                                                                p ->
                                                                                        p.getKind()
                                                                                                .equals(
                                                                                                        "SYNONYM"))
                                                                && s.getPosition() == 1
                                                        ? word.getSynonyms().stream()
                                                                .map(
                                                                        VocabularySynonym
                                                                                ::getSynonymPhrase)
                                                                .toList()
                                                        : phrases(s, "SYNONYM"),
                                                phrases(s, "ANTONYM"),
                                                phrases(s, "COLLOCATION")))
                        .toList());
        response.setFamily(
                word.getFamily().stream()
                        .map(
                                f ->
                                        new VocabularyLearningContent.FamilyMember(
                                                f.getWord(), f.getPartOfSpeech(), f.getMeaning()))
                        .toList());
    }

    private List<String> clean(List<String> values) {
        return values == null
                ? List.of()
                : values.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .toList();
    }

    private void phrases(VocabularySense sense, String kind, List<String> values) {
        clean(values).forEach(p -> sense.getPhrases().add(new VocabularySense.Phrase(kind, p)));
    }

    private List<String> phrases(VocabularySense sense, String kind) {
        return sense.getPhrases().stream()
                .filter(p -> p.getKind().equals(kind))
                .map(VocabularySense.Phrase::getPhrase)
                .toList();
    }
}
