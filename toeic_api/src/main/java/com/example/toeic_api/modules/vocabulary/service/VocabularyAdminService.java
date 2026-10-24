package com.example.toeic_api.modules.vocabulary.service;

import com.example.toeic_api.modules.vocabulary.dto.request.VocabularyCategoryRequest;
import com.example.toeic_api.modules.vocabulary.dto.request.VocabularySetRequest;
import com.example.toeic_api.modules.vocabulary.dto.request.VocabularyWordCreateRequest;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularyCategoryResponse;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularySetResponse;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularyWordResponse;
import com.example.toeic_api.modules.vocabulary.mapper.VocabularyMapper;
import com.example.toeic_api.modules.vocabulary.model.VocabularyCategory;
import com.example.toeic_api.modules.vocabulary.model.VocabularySet;
import com.example.toeic_api.modules.vocabulary.model.VocabularyWord;
import com.example.toeic_api.modules.vocabulary.repository.VocabularyCategoryRepository;
import com.example.toeic_api.modules.vocabulary.repository.VocabularySetRepository;
import com.example.toeic_api.modules.vocabulary.repository.VocabularyWordRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VocabularyAdminService {
    private final VocabularyCategoryRepository vocabularyCategoryRepository;
    private final VocabularySetRepository vocabularySetRepository;
    private final VocabularyWordRepository vocabularyWordRepository;
    private final VocabularyMapper vocabularyMapper;
    private final VocabularyService vocabularyService;
    private final VocabularyContentMapper contentMapper;
    private final com.example.toeic_api.modules.users.service.UserService users;

    public VocabularyCategoryResponse createCategory(VocabularyCategoryRequest request) {
        VocabularyCategory category =
                VocabularyCategory.builder()
                        .createdBy(users.requireUser(null))
                        .categoryName(request.getCategoryName())
                        .description(request.getDescription())
                        .build();
        return vocabularyMapper.toVocabularyCategoryResponse(
                vocabularyCategoryRepository.save(category));
    }

    public VocabularyCategoryResponse updateCategory(Long id, VocabularyCategoryRequest request) {
        VocabularyCategory category = vocabularyService.category(id);
        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        return vocabularyMapper.toVocabularyCategoryResponse(
                vocabularyCategoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        vocabularyService.category(id).setArchived(true);
    }

    public VocabularySetResponse createSet(VocabularySetRequest request) {
        VocabularyCategory category = vocabularyService.category(request.getCategoryId());
        VocabularySet set =
                VocabularySet.builder()
                        .category(category)
                        .setName(request.getSetName())
                        .description(request.getDescription())
                        .build();
        return vocabularyMapper.toVocabularySetResponse(vocabularySetRepository.save(set));
    }

    public VocabularySetResponse updateSet(Long id, VocabularySetRequest request) {
        VocabularySet set = vocabularyService.ownedSet(id);
        VocabularyCategory category = vocabularyService.category(request.getCategoryId());
        set.setCategory(category);
        set.setSetName(request.getSetName());
        set.setDescription(request.getDescription());
        return vocabularyMapper.toVocabularySetResponse(vocabularySetRepository.save(set));
    }

    public void deleteSet(Long id) {
        vocabularyService.ownedSet(id).setArchived(true);
    }

    @Transactional
    public VocabularyWordResponse updateWord(Long id, VocabularyWordCreateRequest request) {
        VocabularyWord word = vocabularyService.ownedWord(id);
        VocabularySet set = vocabularyService.ownedSet(request.getSetId());
        word.getSets().add(set);
        word.setWord(request.getWord());
        word.setPronunciation(request.getPronunciation());
        contentMapper.apply(word, request);
        vocabularyWordRepository.saveAndFlush(word);
        return vocabularyService.getWordById(id);
    }

    public void deleteWord(Long id) {
        vocabularyService.ownedWord(id).setArchived(true);
    }

    public void attachWord(Long setId, Long wordId) {
        var set = vocabularyService.ownedSet(setId);
        var word = vocabularyService.ownedWord(wordId);
        word.getSets().add(set);
    }

    public void detachWord(Long setId, Long wordId) {
        vocabularyService.ownedSet(setId);
        var word = vocabularyService.ownedWord(wordId);
        word.getSets().removeIf(s -> s.getId().equals(setId));
    }
}
