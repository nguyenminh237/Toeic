package com.example.toeic_api.modules.practice.controller;

import com.example.toeic_api.modules.content.dto.response.QuestionView;
import com.example.toeic_api.modules.practice.dto.request.ReadingPartRequest;
import com.example.toeic_api.modules.practice.dto.request.ReadingPassageRequest;
import com.example.toeic_api.modules.practice.dto.request.ReadingQuestionRequest;
import com.example.toeic_api.modules.practice.dto.request.ReadingSectionRequest;
import com.example.toeic_api.modules.practice.dto.response.PracticeViews;
import com.example.toeic_api.modules.practice.service.PracticeAdminService;
import com.example.toeic_api.shared.api.ApiResponse;

import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/reading", "/api/v1/workspace/reading"})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated()")
public class AdminReadingController {

    PracticeAdminService adminService;

    @PostMapping("/parts")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<PracticeViews.Part> createPart(@RequestBody @Valid ReadingPartRequest request) {
        return ApiResponse.<PracticeViews.Part>builder()
                .result(adminService.createReadingPart(request))
                .build();
    }

    @PutMapping("/parts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<PracticeViews.Part> updatePart(
            @PathVariable Long id, @RequestBody @Valid ReadingPartRequest request) {
        return ApiResponse.<PracticeViews.Part>builder()
                .result(adminService.updateReadingPart(id, request))
                .build();
    }

    @DeleteMapping("/parts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Void> deletePart(@PathVariable Long id) {
        adminService.deleteReadingPart(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/sections")
    ApiResponse<PracticeViews.Section> createSection(
            @RequestBody @Valid ReadingSectionRequest request) {
        return ApiResponse.<PracticeViews.Section>builder()
                .result(adminService.createReadingSection(request))
                .build();
    }

    @PutMapping("/sections/{id}")
    ApiResponse<PracticeViews.Section> updateSection(
            @PathVariable Long id, @RequestBody @Valid ReadingSectionRequest request) {
        return ApiResponse.<PracticeViews.Section>builder()
                .result(adminService.updateReadingSection(id, request))
                .build();
    }

    @DeleteMapping("/sections/{id}")
    ApiResponse<Void> deleteSection(@PathVariable Long id) {
        adminService.deleteReadingSection(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/passages")
    ApiResponse<List<PracticeViews.Passage>> getPassages(@RequestParam Long sectionId) {
        return ApiResponse.<List<PracticeViews.Passage>>builder()
                .result(adminService.getPassagesBySection(sectionId))
                .build();
    }

    @PostMapping("/passages")
    ApiResponse<PracticeViews.Passage> createPassage(
            @RequestBody @Valid ReadingPassageRequest request) {
        return ApiResponse.<PracticeViews.Passage>builder()
                .result(adminService.createPassage(request))
                .build();
    }

    @PutMapping("/passages/{id}")
    ApiResponse<PracticeViews.Passage> updatePassage(
            @PathVariable Long id, @RequestBody @Valid ReadingPassageRequest request) {
        return ApiResponse.<PracticeViews.Passage>builder()
                .result(adminService.updatePassage(id, request))
                .build();
    }

    @DeleteMapping("/passages/{id}")
    ApiResponse<Void> deletePassage(@PathVariable Long id) {
        adminService.deletePassage(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/questions")
    ApiResponse<QuestionView> createQuestion(@RequestBody @Valid ReadingQuestionRequest request) {
        return ApiResponse.<QuestionView>builder()
                .result(adminService.createReadingQuestion(request))
                .build();
    }

    @PutMapping("/questions/{id}")
    ApiResponse<QuestionView> updateQuestion(
            @PathVariable Long id, @RequestBody @Valid ReadingQuestionRequest request) {
        return ApiResponse.<QuestionView>builder()
                .result(adminService.updateReadingQuestion(id, request))
                .build();
    }

    @DeleteMapping("/questions/{id}")
    ApiResponse<Void> deleteQuestion(@PathVariable Long id) {
        adminService.deleteReadingQuestion(id);
        return ApiResponse.<Void>builder().build();
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.example.toeic_api.modules.practice.service.PracticeService practice;

    @GetMapping("/questions")
    public ApiResponse<java.util.List<QuestionView>> listQuestions(@RequestParam Long sectionId) {
        return ApiResponse.<java.util.List<QuestionView>>builder()
                .result(practice.questions(sectionId, false, true))
                .build();
    }
}
