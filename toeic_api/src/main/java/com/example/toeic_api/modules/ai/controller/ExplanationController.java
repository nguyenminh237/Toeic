package com.example.toeic_api.modules.ai.controller;

import com.example.toeic_api.modules.ai.dto.request.ExplanationRequest;
import com.example.toeic_api.modules.ai.service.ExplanationService;
import com.example.toeic_api.shared.api.ApiResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/v1/admin/content", "/api/v1/workspace/content"})
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ExplanationController {
    private final ExplanationService explanations;

    @PostMapping("/explanations")
    public ApiResponse<Map<String, String>> generate(
            @RequestBody @Valid ExplanationRequest request) {
        return ApiResponse.<Map<String, String>>builder()
                .result(Map.of("explanation", explanations.generate(request)))
                .build();
    }
}
