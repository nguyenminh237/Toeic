package com.example.toeic_api.modules.tests.controller;

import com.example.toeic_api.modules.content.dto.response.QuestionView;
import com.example.toeic_api.modules.tests.dto.request.TestPartRequest;
import com.example.toeic_api.modules.tests.dto.request.TestQuestionRequest;
import com.example.toeic_api.modules.tests.dto.request.TestSuiteRequest;
import com.example.toeic_api.modules.tests.dto.response.TestPartResponse;
import com.example.toeic_api.modules.tests.dto.response.TestSuiteResponse;
import com.example.toeic_api.modules.tests.service.TestAdminService;
import com.example.toeic_api.shared.api.ApiResponse;

import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/tests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminTestController {

    TestAdminService adminService;

    @PostMapping("/suites")
    ApiResponse<TestSuiteResponse> createSuite(@RequestBody @Valid TestSuiteRequest request) {
        return ApiResponse.<TestSuiteResponse>builder()
                .result(adminService.createSuite(request))
                .build();
    }

    @PutMapping("/suites/{id}")
    ApiResponse<TestSuiteResponse> updateSuite(
            @PathVariable Long id, @RequestBody @Valid TestSuiteRequest request) {
        return ApiResponse.<TestSuiteResponse>builder()
                .result(adminService.updateSuite(id, request))
                .build();
    }

    @DeleteMapping("/suites/{id}")
    ApiResponse<Void> deleteSuite(@PathVariable Long id) {
        adminService.deleteSuite(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/parts")
    ApiResponse<TestPartResponse> createPart(@RequestBody @Valid TestPartRequest request) {
        return ApiResponse.<TestPartResponse>builder()
                .result(adminService.createTestPart(request))
                .build();
    }

    @PutMapping("/parts/{id}")
    ApiResponse<TestPartResponse> updatePart(
            @PathVariable Long id, @RequestBody @Valid TestPartRequest request) {
        return ApiResponse.<TestPartResponse>builder()
                .result(adminService.updateTestPart(id, request))
                .build();
    }

    @DeleteMapping("/parts/{id}")
    ApiResponse<Void> deletePart(@PathVariable Long id) {
        adminService.deleteTestPart(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/questions")
    ApiResponse<QuestionView> createQuestion(@RequestBody @Valid TestQuestionRequest request) {
        return ApiResponse.<QuestionView>builder()
                .result(adminService.createTestQuestion(request))
                .build();
    }

    @PutMapping("/questions/{id}")
    ApiResponse<QuestionView> updateQuestion(
            @PathVariable Long id, @RequestBody @Valid TestQuestionRequest request) {
        return ApiResponse.<QuestionView>builder()
                .result(adminService.updateTestQuestion(id, request))
                .build();
    }

    @DeleteMapping("/questions/{id}")
    ApiResponse<Void> deleteQuestion(@PathVariable Long id) {
        adminService.deleteTestQuestion(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/results")
    ApiResponse<com.example.toeic_api.modules.tests.dto.response.HistoryPage> getResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.<com.example.toeic_api.modules.tests.dto.response.HistoryPage>builder()
                .result(adminService.getAllResults(page, size))
                .build();
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.example.toeic_api.modules.tests.service.TestService tests;

    @GetMapping("/questions")
    public ApiResponse<java.util.List<QuestionView>> listQuestions(@RequestParam Long partId) {
        return ApiResponse.<java.util.List<QuestionView>>builder()
                .result(tests.questions(partId, true))
                .build();
    }
}
