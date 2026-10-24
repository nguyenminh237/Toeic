package com.example.toeic_api.modules.practice.controller;

import com.example.toeic_api.modules.content.dto.response.QuestionView;
import com.example.toeic_api.modules.practice.dto.request.ListeningPartRequest;
import com.example.toeic_api.modules.practice.dto.request.ListeningQuestionCreateRequest;
import com.example.toeic_api.modules.practice.dto.request.ListeningSectionRequest;
import com.example.toeic_api.modules.practice.dto.response.PracticeViews;
import com.example.toeic_api.modules.practice.service.PracticeAdminService;
import com.example.toeic_api.shared.api.ApiResponse;

import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/admin/listening", "/api/v1/workspace/listening"})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated()")
public class AdminListeningController {

    PracticeAdminService adminService;

    @PostMapping("/parts")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<PracticeViews.Part> createPart(@RequestBody @Valid ListeningPartRequest request) {
        return ApiResponse.<PracticeViews.Part>builder()
                .result(adminService.createListeningPart(request))
                .build();
    }

    @PutMapping("/parts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<PracticeViews.Part> updatePart(
            @PathVariable Long id, @RequestBody @Valid ListeningPartRequest request) {
        return ApiResponse.<PracticeViews.Part>builder()
                .result(adminService.updateListeningPart(id, request))
                .build();
    }

    @DeleteMapping("/parts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Void> deletePart(@PathVariable Long id) {
        adminService.deleteListeningPart(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/sections")
    ApiResponse<PracticeViews.Section> createSection(
            @RequestBody @Valid ListeningSectionRequest request) {
        return ApiResponse.<PracticeViews.Section>builder()
                .result(adminService.createListeningSection(request))
                .build();
    }

    @PutMapping("/sections/{id}")
    ApiResponse<PracticeViews.Section> updateSection(
            @PathVariable Long id, @RequestBody @Valid ListeningSectionRequest request) {
        return ApiResponse.<PracticeViews.Section>builder()
                .result(adminService.updateListeningSection(id, request))
                .build();
    }

    @DeleteMapping("/sections/{id}")
    ApiResponse<Void> deleteSection(@PathVariable Long id) {
        adminService.deleteListeningSection(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/questions")
    ApiResponse<QuestionView> createQuestion(
            @RequestBody @Valid ListeningQuestionCreateRequest request) {
        return ApiResponse.<QuestionView>builder()
                .result(adminService.createListeningQuestion(request))
                .build();
    }

    @PutMapping("/questions/{id}")
    ApiResponse<QuestionView> updateQuestion(
            @PathVariable Long id, @RequestBody @Valid ListeningQuestionCreateRequest request) {
        return ApiResponse.<QuestionView>builder()
                .result(adminService.updateListeningQuestion(id, request))
                .build();
    }

    @DeleteMapping("/questions/{id}")
    ApiResponse<Void> deleteQuestion(@PathVariable Long id) {
        adminService.deleteListeningQuestion(id);
        return ApiResponse.<Void>builder().build();
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.example.toeic_api.modules.practice.service.PracticeService practice;

    @GetMapping("/questions")
    public ApiResponse<java.util.List<QuestionView>> listQuestions(@RequestParam Long sectionId) {
        return ApiResponse.<java.util.List<QuestionView>>builder()
                .result(practice.questions(sectionId, true, true))
                .build();
    }
}
