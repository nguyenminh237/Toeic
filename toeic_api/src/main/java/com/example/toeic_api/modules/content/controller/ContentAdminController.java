package com.example.toeic_api.modules.content.controller;

import com.example.toeic_api.modules.content.dto.request.ContentBundleRequest;
import com.example.toeic_api.modules.content.dto.request.ContentRequests;
import com.example.toeic_api.modules.content.dto.response.ContentBundleResponse;
import com.example.toeic_api.modules.content.dto.response.QuestionView;
import com.example.toeic_api.modules.content.mapper.ContentViews;
import com.example.toeic_api.modules.content.service.ContentBundleService;
import com.example.toeic_api.modules.content.service.ContentService;
import com.example.toeic_api.modules.practice.service.PracticeAdminService;
import com.example.toeic_api.modules.tests.service.TestAdminService;
import com.example.toeic_api.shared.api.ApiResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping({"/api/v1/admin/content", "/api/v1/workspace/content"})
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Transactional
public class ContentAdminController {
    private final ContentService content;
    private final ContentViews views;
    private final PracticeAdminService practice;
    private final TestAdminService tests;
    private final ContentBundleService bundles;

    @PostMapping("/batches")
    public ApiResponse<List<ContentBundleResponse>> batch(
            @RequestBody @Valid
                    com.example.toeic_api.modules.content.dto.request.ContentBatchRequest request) {
        return ok(bundles.saveBatch(request));
    }

    @PostMapping("/bundles")
    public ApiResponse<ContentBundleResponse> bundle(
            @RequestBody @Valid ContentBundleRequest request) {
        return ok(bundles.save(null, request));
    }

    @PutMapping("/groups/{id}/bundle")
    public ApiResponse<ContentBundleResponse> bundle(
            @PathVariable Long id, @RequestBody @Valid ContentBundleRequest request) {
        return ok(bundles.save(id, request));
    }

    private <T> ApiResponse<T> ok(T v) {
        return ApiResponse.<T>builder().result(v).build();
    }

    @GetMapping("/groups/{id}/questions")
    public ApiResponse<List<QuestionView>> questions(@PathVariable Long id) {
        return ok(
                content.questions(id).stream()
                        .map(q -> views.question(q, q.getPosition(), true))
                        .toList());
    }

    @PutMapping("/sets/{id}/groups/{groupId}")
    public ApiResponse<Void> attachPracticeGroup(
            @PathVariable Long id,
            @PathVariable Long groupId,
            @RequestBody @Valid ContentRequests.GroupMembership r) {
        int position = r.position();
        for (var q : content.questions(groupId)) practice.attach(id, q.getId(), position++);
        return ok(null);
    }

    @PutMapping("/test-parts/{id}/groups/{groupId}")
    public ApiResponse<Void> attachTestGroup(
            @PathVariable Long id,
            @PathVariable Long groupId,
            @RequestBody @Valid ContentRequests.GroupMembership r) {
        int position = r.position();
        for (var q : content.questions(groupId)) tests.attach(id, q.getId(), position++);
        return ok(null);
    }

    @GetMapping("/groups")
    public ApiResponse<List<Map<String, Object>>> groups(@RequestParam int partNumber) {
        return ok(
                content.listGroups(partNumber).stream()
                        .map(
                                g -> {
                                    Map<String, Object> m = new LinkedHashMap<>();
                                    m.put("id", g.getId());
                                    m.put("version", g.getVersion());
                                    m.put("partNumber", g.getPart().getId());
                                    m.put("title", g.getTitle());
                                    m.put("audioUrl", g.getAudioUrl());
                                    m.put("imageUrl", g.getImageUrl());
                                    m.put("documents", new ArrayList<>(g.getDocuments()));
                                    m.put("audioStartMs", g.getAudioStartMs());
                                    m.put("audioEndMs", g.getAudioEndMs());
                                    m.put("transcript", g.getTranscript());
                                    return m;
                                })
                        .toList());
    }

    @PostMapping("/groups")
    public ApiResponse<Map<String, Long>> group(@RequestBody @Valid ContentRequests.Group r) {
        return ok(Map.of("id", content.saveGroup(null, r).getId()));
    }

    @PutMapping("/groups/{id}")
    public ApiResponse<Map<String, Long>> group(
            @PathVariable Long id, @RequestBody @Valid ContentRequests.Group r) {
        return ok(Map.of("id", content.saveGroup(id, r).getId()));
    }

    @DeleteMapping("/groups/{id}")
    public ApiResponse<Void> archiveGroup(@PathVariable Long id) {
        content.archiveGroup(id);
        return ok(null);
    }

    @PostMapping("/questions")
    public ApiResponse<QuestionView> question(@RequestBody @Valid ContentRequests.Question r) {
        var q = content.saveQuestion(null, r);
        return ok(views.question(q, q.getPosition(), true));
    }

    @PutMapping("/questions/{id}")
    public ApiResponse<QuestionView> question(
            @PathVariable Long id, @RequestBody @Valid ContentRequests.Question r) {
        var q = content.saveQuestion(id, r);
        return ok(views.question(q, q.getPosition(), true));
    }

    @DeleteMapping("/questions/{id}")
    public ApiResponse<Void> archive(@PathVariable Long id) {
        content.archiveQuestion(id);
        return ok(null);
    }

    @PutMapping("/sets/{id}/items")
    public ApiResponse<Void> setItem(
            @PathVariable Long id, @RequestBody @Valid ContentRequests.Membership r) {
        practice.attach(id, r.questionId(), r.position());
        return ok(null);
    }

    @DeleteMapping("/sets/{id}/items/{questionId}")
    public ApiResponse<Void> removeSetItem(@PathVariable Long id, @PathVariable Long questionId) {
        practice.detach(id, questionId);
        return ok(null);
    }

    @PutMapping("/test-parts/{id}/items")
    public ApiResponse<Void> testItem(
            @PathVariable Long id, @RequestBody @Valid ContentRequests.Membership r) {
        tests.attach(id, r.questionId(), r.position());
        return ok(null);
    }

    @DeleteMapping("/test-parts/{id}/items/{questionId}")
    public ApiResponse<Void> removeTestItem(@PathVariable Long id, @PathVariable Long questionId) {
        tests.detach(id, questionId);
        return ok(null);
    }
}
