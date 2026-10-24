package com.example.toeic_api.modules.practice.controller;

import com.example.toeic_api.modules.content.dto.response.QuestionView;
import com.example.toeic_api.modules.practice.dto.request.ListeningAnswerRequest;
import com.example.toeic_api.modules.practice.dto.request.ProgressStatusRequest;
import com.example.toeic_api.modules.practice.dto.response.PracticeViews;
import com.example.toeic_api.modules.practice.service.PracticeService;
import com.example.toeic_api.shared.api.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/listening")
public class ListeningController {
    private final PracticeService service;

    private <T> ApiResponse<T> ok(T v) {
        return ApiResponse.<T>builder().result(v).build();
    }

    @GetMapping("/parts")
    public ApiResponse<List<PracticeViews.Part>> parts() {
        return ok(service.parts(true));
    }

    @GetMapping("/parts/{partNumber}")
    public ApiResponse<PracticeViews.Part> part(@PathVariable int partNumber) {
        return ok(service.part(partNumber, true));
    }

    @GetMapping("/sections/{partNumber}")
    public ApiResponse<List<PracticeViews.Section>> sections(@PathVariable int partNumber) {
        return ok(service.sections(partNumber, true));
    }

    @GetMapping("/questions/section/{sectionId}")
    public ApiResponse<List<QuestionView>> questions(@PathVariable Long sectionId) {
        return ok(service.questions(sectionId, true, false));
    }

    @GetMapping("/questions/{id}")
    public ApiResponse<QuestionView> question(@PathVariable Long id) {
        return ok(service.question(id, true));
    }

    @GetMapping("/progress/{userId}")
    public ApiResponse<List<PracticeViews.Progress>> progress(@PathVariable String userId) {
        return ok(service.progress(true));
    }

    @PostMapping("/answer")
    public ApiResponse<PracticeViews.Progress> answer(@RequestBody ListeningAnswerRequest r) {
        return ok(service.answer(r.getQuestionId(), r.getSelectedAnswer(), true));
    }

    @PatchMapping("/progress")
    public ApiResponse<PracticeViews.Progress> status(@RequestBody ProgressStatusRequest r) {
        return ok(service.status(r.getQuestionId(), r.getStatus(), r.getBookmarked(), true));
    }

    @GetMapping("/parts/{partNumber}/summary")
    public ApiResponse<List<PracticeViews.Summary>> summary(@PathVariable int partNumber) {
        return ok(service.summaries(partNumber, true));
    }
}
