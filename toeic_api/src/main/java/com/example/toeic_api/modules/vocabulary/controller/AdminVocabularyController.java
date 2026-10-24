package com.example.toeic_api.modules.vocabulary.controller;

import com.example.toeic_api.modules.vocabulary.dto.request.VocabularyCategoryRequest;
import com.example.toeic_api.modules.vocabulary.dto.request.VocabularySetRequest;
import com.example.toeic_api.modules.vocabulary.dto.request.VocabularyWordCreateRequest;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularyCategoryResponse;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularySetResponse;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularyWordResponse;
import com.example.toeic_api.modules.vocabulary.service.VocabularyAdminService;
import com.example.toeic_api.modules.vocabulary.service.VocabularyService;
import com.example.toeic_api.shared.api.ApiResponse;

import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/admin/vocabulary", "/api/v1/workspace/vocabulary"})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated()")
public class AdminVocabularyController {

    VocabularyAdminService adminService;

    @PutMapping("/sets/{setId}/words/{wordId}")
    public ApiResponse<Void> attachWord(@PathVariable Long setId, @PathVariable Long wordId) {
        adminService.attachWord(setId, wordId);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/sets/{setId}/words/{wordId}")
    public ApiResponse<Void> detachWord(@PathVariable Long setId, @PathVariable Long wordId) {
        adminService.detachWord(setId, wordId);
        return ApiResponse.<Void>builder().build();
    }

    VocabularyService vocabularyService;

    @PostMapping("/categories")
    ApiResponse<VocabularyCategoryResponse> createCategory(
            @RequestBody @Valid VocabularyCategoryRequest request) {
        return ApiResponse.<VocabularyCategoryResponse>builder()
                .result(adminService.createCategory(request))
                .build();
    }

    @PutMapping("/categories/{id}")
    ApiResponse<VocabularyCategoryResponse> updateCategory(
            @PathVariable Long id, @RequestBody @Valid VocabularyCategoryRequest request) {
        return ApiResponse.<VocabularyCategoryResponse>builder()
                .result(adminService.updateCategory(id, request))
                .build();
    }

    @DeleteMapping("/categories/{id}")
    ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/sets")
    ApiResponse<VocabularySetResponse> createSet(@RequestBody @Valid VocabularySetRequest request) {
        return ApiResponse.<VocabularySetResponse>builder()
                .result(adminService.createSet(request))
                .build();
    }

    @PutMapping("/sets/{id}")
    ApiResponse<VocabularySetResponse> updateSet(
            @PathVariable Long id, @RequestBody @Valid VocabularySetRequest request) {
        return ApiResponse.<VocabularySetResponse>builder()
                .result(adminService.updateSet(id, request))
                .build();
    }

    @DeleteMapping("/sets/{id}")
    ApiResponse<Void> deleteSet(@PathVariable Long id) {
        adminService.deleteSet(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/words")
    ApiResponse<VocabularyWordResponse> createWord(
            @RequestBody @Valid VocabularyWordCreateRequest request) {
        return ApiResponse.<VocabularyWordResponse>builder()
                .result(vocabularyService.createWord(request))
                .build();
    }

    @PutMapping("/words/{id}")
    ApiResponse<VocabularyWordResponse> updateWord(
            @PathVariable Long id, @RequestBody @Valid VocabularyWordCreateRequest request) {
        return ApiResponse.<VocabularyWordResponse>builder()
                .result(adminService.updateWord(id, request))
                .build();
    }

    @DeleteMapping("/words/{id}")
    ApiResponse<Void> deleteWord(@PathVariable Long id) {
        adminService.deleteWord(id);
        return ApiResponse.<Void>builder().build();
    }
}
