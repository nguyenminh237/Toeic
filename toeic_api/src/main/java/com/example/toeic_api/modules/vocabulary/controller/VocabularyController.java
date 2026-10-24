package com.example.toeic_api.modules.vocabulary.controller;

import com.example.toeic_api.modules.vocabulary.dto.request.VocabularyWordCreateRequest;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularyCategoryResponse;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularySetResponse;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularyWordResponse;
import com.example.toeic_api.modules.vocabulary.service.VocabularyService;
import com.example.toeic_api.shared.api.ApiResponse;

import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vocabulary")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VocabularyController {

    VocabularyService vocabularyService;

    @GetMapping("/categories")
    ApiResponse<List<VocabularyCategoryResponse>> getAllCategories() {
        return ApiResponse.<List<VocabularyCategoryResponse>>builder()
                .result(vocabularyService.getAllCategories())
                .build();
    }

    @GetMapping("/categories/{categoryId}")
    ApiResponse<VocabularyCategoryResponse> getCategoryById(@PathVariable Long categoryId) {
        return ApiResponse.<VocabularyCategoryResponse>builder()
                .result(vocabularyService.getCategoryById(categoryId))
                .build();
    }

    @GetMapping("/sets/{categoryId}")
    ApiResponse<List<VocabularySetResponse>> getSetsByCategory(@PathVariable Long categoryId) {
        return ApiResponse.<List<VocabularySetResponse>>builder()
                .result(vocabularyService.getSetsByCategory(categoryId))
                .build();
    }

    @GetMapping("/sets/{setId}/words")
    ApiResponse<List<VocabularyWordResponse>> getWordsBySet(@PathVariable Long setId) {
        return ApiResponse.<List<VocabularyWordResponse>>builder()
                .result(vocabularyService.getWordsBySet(setId))
                .build();
    }

    @GetMapping("/words/{wordId}")
    ApiResponse<VocabularyWordResponse> getWordById(@PathVariable Long wordId) {
        return ApiResponse.<VocabularyWordResponse>builder()
                .result(vocabularyService.getWordById(wordId))
                .build();
    }

    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    @PostMapping("/words")
    ApiResponse<VocabularyWordResponse> createWord(
            @RequestBody @Valid VocabularyWordCreateRequest request) {
        return ApiResponse.<VocabularyWordResponse>builder()
                .result(vocabularyService.createWord(request))
                .build();
    }

    @GetMapping("/sets/{id}/detail")
    public ApiResponse<VocabularySetResponse> set(@PathVariable Long id) {
        return ApiResponse.<VocabularySetResponse>builder()
                .result(vocabularyService.getSetById(id))
                .build();
    }
}
