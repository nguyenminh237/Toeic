package com.example.toeic_api.modules.vocabulary.controller;

import com.example.toeic_api.modules.ai.service.GroqVocabularyService;
import com.example.toeic_api.modules.vocabulary.dto.request.*;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularyWordResponse;
import com.example.toeic_api.modules.vocabulary.service.*;
import com.example.toeic_api.shared.api.ApiResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/admin/vocabulary/import", "/api/v1/workspace/vocabulary/import"})
@PreAuthorize("isAuthenticated()")
public class VocabularyImportController {
    private final VocabularyDocumentService documents;
    private final GroqVocabularyService ai;
    private final VocabularyImportService imports;

    @PostMapping(value = "/extract", consumes = "multipart/form-data")
    public ApiResponse<VocabularyDocumentService.Extraction> extract(
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "1") int fromPage,
            @RequestParam(defaultValue = "3") int toPage) {
        return ApiResponse.<VocabularyDocumentService.Extraction>builder()
                .result(documents.extract(file, fromPage, toPage))
                .build();
    }

    @PostMapping("/generate")
    public ApiResponse<List<VocabularyWordCreateRequest>> generate(
            @RequestBody @Valid VocabularyImportRequests.Generate request) {
        return ApiResponse.<List<VocabularyWordCreateRequest>>builder()
                .result(ai.generate(request))
                .build();
    }

    @PostMapping("/confirm")
    public ApiResponse<List<VocabularyWordResponse>> confirm(
            @RequestBody @Valid VocabularyImportRequests.Confirm request) {
        return ApiResponse.<List<VocabularyWordResponse>>builder()
                .result(imports.confirm(request))
                .build();
    }
}
