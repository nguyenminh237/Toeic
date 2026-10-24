package com.example.toeic_api.modules.tests.controller;

import com.example.toeic_api.modules.content.dto.response.QuestionView;
import com.example.toeic_api.modules.tests.dto.request.TestSubmitRequest;
import com.example.toeic_api.modules.tests.dto.response.TestPartResponse;
import com.example.toeic_api.modules.tests.dto.response.TestSuiteResponse;
import com.example.toeic_api.modules.tests.dto.response.TestViews;
import com.example.toeic_api.modules.tests.service.TestService;
import com.example.toeic_api.shared.api.ApiResponse;

import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tests")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TestController {

    TestService testService;

    @GetMapping("/suites")
    ApiResponse<List<TestSuiteResponse>> getAllSuites() {
        return ApiResponse.<List<TestSuiteResponse>>builder()
                .result(testService.getAllSuites())
                .build();
    }

    @GetMapping("/suites/{suiteId}")
    ApiResponse<TestSuiteResponse> getSuiteById(@PathVariable Long suiteId) {
        return ApiResponse.<TestSuiteResponse>builder()
                .result(testService.getSuiteById(suiteId))
                .build();
    }

    @GetMapping("/suites/{suiteId}/parts")
    ApiResponse<List<TestPartResponse>> getPartsBySuite(@PathVariable Long suiteId) {
        return ApiResponse.<List<TestPartResponse>>builder()
                .result(testService.getPartsBySuite(suiteId))
                .build();
    }

    @GetMapping("/parts/{partId}")
    ApiResponse<TestPartResponse> getPartById(@PathVariable Long partId) {
        return ApiResponse.<TestPartResponse>builder()
                .result(testService.getPartById(partId))
                .build();
    }

    @GetMapping("/parts/{partId}/questions")
    ApiResponse<List<QuestionView>> getQuestionsByPart(@PathVariable Long partId) {
        return ApiResponse.<List<QuestionView>>builder()
                .result(testService.getQuestionsByPart(partId))
                .build();
    }

    @GetMapping("/questions/{questionId}")
    ApiResponse<QuestionView> getQuestionById(@PathVariable Long questionId) {
        return ApiResponse.<QuestionView>builder()
                .result(testService.getQuestionById(questionId))
                .build();
    }

    @PostMapping("/start/{partId}")
    ApiResponse<TestViews.Attempt> startTest(
            @PathVariable Long partId, @RequestParam(required = false) String userId) {
        return ApiResponse.<TestViews.Attempt>builder()
                .result(testService.startTest(userId, partId))
                .build();
    }

    @PostMapping("/submit")
    ApiResponse<TestViews.Attempt> submitTest(
            @RequestParam(required = false) String userId,
            @RequestBody @Valid TestSubmitRequest request) {
        return ApiResponse.<TestViews.Attempt>builder()
                .result(testService.submitTest(userId, request))
                .build();
    }

    @GetMapping({"/results/{userId}", "/results"})
    ApiResponse<com.example.toeic_api.modules.tests.dto.response.HistoryPage> getUserTestResults(
            @PathVariable(required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.<com.example.toeic_api.modules.tests.dto.response.HistoryPage>builder()
                .result(testService.getUserTestResults(userId, page, size))
                .build();
    }

    @GetMapping("/results/detail/{resultId}")
    ApiResponse<TestViews.Attempt> getTestResult(@PathVariable Long resultId) {
        return ApiResponse.<TestViews.Attempt>builder()
                .result(testService.getTestResult(resultId))
                .build();
    }

    @PutMapping("/attempts/{id}/answers")
    public ApiResponse<TestViews.Attempt> saveDraft(
            @PathVariable Long id,
            @RequestBody
                    java.util.List<
                                    com.example.toeic_api.modules.tests.dto.request
                                            .TestAnswerRequest>
                            answers) {
        return ApiResponse.<TestViews.Attempt>builder()
                .result(testService.draft(id, answers))
                .build();
    }

    @GetMapping("/me/summary")
    public ApiResponse<java.util.Map<String, Long>> summary() {
        return ApiResponse.<java.util.Map<String, Long>>builder()
                .result(testService.summary())
                .build();
    }
}
