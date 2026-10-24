package com.example.toeic_api.modules.vocabulary.service;

import com.example.toeic_api.modules.vocabulary.dto.request.VocabularyWordCreateRequest;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularyCategoryResponse;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularySetResponse;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularyWordResponse;
import com.example.toeic_api.modules.vocabulary.mapper.VocabularyMapper;
import com.example.toeic_api.modules.vocabulary.model.VocabularyCategory;
import com.example.toeic_api.modules.vocabulary.model.VocabularySet;
import com.example.toeic_api.modules.vocabulary.model.VocabularySynonym;
import com.example.toeic_api.modules.vocabulary.model.VocabularyWord;
import com.example.toeic_api.modules.vocabulary.repository.VocabularyCategoryRepository;
import com.example.toeic_api.modules.vocabulary.repository.VocabularySetRepository;
import com.example.toeic_api.modules.vocabulary.repository.VocabularyWordRepository;
import com.example.toeic_api.shared.exception.AppException;
import com.example.toeic_api.shared.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.transaction.annotation.Transactional
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VocabularyService {

    VocabularyCategoryRepository categoryRepository;
    VocabularySetRepository setRepository;
    VocabularyWordRepository wordRepository;
    VocabularyMapper mapper;
    VocabularyContentMapper contentMapper;
    com.example.toeic_api.shared.security.ContentOwnership ownership;

    // Category methods
    public List<VocabularyCategoryResponse> getAllCategories() {
        return categoryRepository
                .findByCreatedByIdAndArchivedFalseOrderByIdAsc(ownership.currentId())
                .stream()
                .map(mapper::toVocabularyCategoryResponse)
                .toList();
    }

    public VocabularyCategory category(Long id) {
        return categoryRepository
                .findById(id)
                .filter(
                        c ->
                                !c.isArchived()
                                        && c.getCreatedBy() != null
                                        && ownership.owns(c.getCreatedBy().getId()))
                .orElseThrow(() -> new AppException(ErrorCode.VOCABULARY_CATEGORY_NOT_FOUND));
    }

    public VocabularyCategoryResponse getCategoryById(Long id) {
        return mapper.toVocabularyCategoryResponse(category(id));
    }

    // Set methods
    public List<VocabularySetResponse> getSetsByCategory(Long categoryId) {
        category(categoryId);
        return setRepository.findByCategoryId(categoryId).stream()
                .filter(x -> !x.isArchived())
                .map(mapper::toVocabularySetResponse)
                .toList();
    }

    public VocabularySet ownedSet(Long id) {
        var set =
                setRepository
                        .findById(id)
                        .filter(x -> !x.isArchived() && !x.getCategory().isArchived())
                        .orElseThrow(() -> new AppException(ErrorCode.VOCABULARY_SET_NOT_FOUND));
        category(set.getCategory().getId());
        return set;
    }

    public VocabularySetResponse getSetById(Long id) {
        return mapper.toVocabularySetResponse(ownedSet(id));
    }

    // Word methods
    public List<VocabularyWordResponse> getWordsBySet(Long setId) {
        ownedSet(setId);
        return wordRepository.findBySetIdAndOwner(setId, ownership.currentId()).stream()
                .map(
                        word -> {
                            var response = enrichWordWithSynonyms(word);
                            response.setSetId(setId);
                            return response;
                        })
                .toList();
    }

    public VocabularyWord ownedWord(Long id) {
        return wordRepository
                .findById(id)
                .filter(w -> !w.isArchived() && ownership.owns(w.getOwnerId()))
                .orElseThrow(() -> new AppException(ErrorCode.VOCABULARY_WORD_NOT_FOUND));
    }

    public VocabularyWordResponse getWordById(Long id) {
        return enrichWordWithSynonyms(ownedWord(id));
    }

    public VocabularyWordResponse createWord(VocabularyWordCreateRequest request) {
        VocabularySet set = ownedSet(request.getSetId());

        VocabularyWord word =
                VocabularyWord.builder()
                        .sets(new java.util.HashSet<>(java.util.Set.of(set)))
                        .ownerId(ownership.currentId())
                        .word(request.getWord())
                        .meaning(request.getMeaning())
                        .partOfSpeech(request.getPartOfSpeech())
                        .exampleSentence(request.getExampleSentence())
                        .exampleTranslation(request.getExampleTranslation())
                        .pronunciation(request.getPronunciation())
                        .synonyms(new java.util.HashSet<>())
                        .build();

        contentMapper.apply(word, request);
        return enrichWordWithSynonyms(wordRepository.saveAndFlush(word));
    }

    private VocabularyWordResponse enrichWordWithSynonyms(VocabularyWord word) {
        VocabularyWordResponse response = mapper.toVocabularyWordResponse(word);
        contentMapper.enrich(word, response);
        response.setSynonyms(
                word.getSynonyms() == null
                        ? java.util.Set.of()
                        : word.getSynonyms().stream()
                                .map(VocabularySynonym::getSynonymPhrase)
                                .collect(Collectors.toSet()));
        return response;
    }
}
