package com.example.toeic_api.modules.tests.service;

import com.example.toeic_api.modules.content.dto.response.QuestionView;
import com.example.toeic_api.modules.content.mapper.ContentViews;
import com.example.toeic_api.modules.content.service.ContentService;
import com.example.toeic_api.modules.tests.dto.request.TestAnswerRequest;
import com.example.toeic_api.modules.tests.dto.request.TestSubmitRequest;
import com.example.toeic_api.modules.tests.dto.response.HistoryPage;
import com.example.toeic_api.modules.tests.dto.response.TestPartResponse;
import com.example.toeic_api.modules.tests.dto.response.TestSuiteResponse;
import com.example.toeic_api.modules.tests.dto.response.TestViews;
import com.example.toeic_api.modules.tests.model.AttemptItem;
import com.example.toeic_api.modules.tests.model.TestAttempt;
import com.example.toeic_api.modules.tests.model.TestPart;
import com.example.toeic_api.modules.tests.model.TestPartItem;
import com.example.toeic_api.modules.tests.model.TestSuite;
import com.example.toeic_api.modules.tests.repository.TestAttemptRepository;
import com.example.toeic_api.modules.tests.repository.TestPartItemRepository;
import com.example.toeic_api.modules.tests.repository.TestPartRepository;
import com.example.toeic_api.modules.tests.repository.TestSuiteRepository;
import com.example.toeic_api.modules.users.model.User;
import com.example.toeic_api.modules.users.repository.UserRepository;
import com.example.toeic_api.modules.users.service.UserService;
import com.example.toeic_api.shared.exception.AppException;
import com.example.toeic_api.shared.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestService {
    private final TestSuiteRepository suites;
    private final TestPartRepository parts;
    private final TestPartItemRepository items;
    private final TestAttemptRepository attempts;
    private final UserRepository userRepository;
    private final UserService users;
    private final ContentService content;
    private final ContentViews views;
    private final ObjectMapper json;
    private final Clock clock;
    private final com.example.toeic_api.shared.security.ContentOwnership ownership;

    public TestSuite suite(Long id) {
        return suites.findById(id)
                .filter(s -> !s.isArchived() && ownership.owns(s.getOwnerId()))
                .orElseThrow(() -> new AppException(ErrorCode.TEST_SUITE_NOT_FOUND));
    }

    public TestPart part(Long id) {
        return parts.findById(id)
                .filter(
                        p ->
                                !p.isArchived()
                                        && !p.getSuite().isArchived()
                                        && ownership.owns(p.getSuite().getOwnerId()))
                .orElseThrow(() -> new AppException(ErrorCode.TEST_PART_NOT_FOUND));
    }

    public TestSuiteResponse suiteView(TestSuite s) {
        return TestSuiteResponse.builder()
                .id(s.getId())
                .suiteName(s.getSuiteName())
                .source(s.getSource())
                .description(s.getDescription())
                .createdAt(s.getCreatedAt())
                .build();
    }

    public TestPartResponse partView(TestPart p) {
        return TestPartResponse.builder()
                .id(p.getId())
                .suiteId(p.getSuite().getId())
                .partNumber(p.getToeicPart().getId())
                .partType(p.getToeicPart().isListening() ? "listening" : "reading")
                .questionCount(Math.toIntExact(items.countActive(p.getId())))
                .timeLimit(p.getTimeLimit())
                .audioUrl(p.getAudioUrl())
                .createdAt(p.getCreatedAt())
                .build();
    }

    public List<TestSuiteResponse> getAllSuites() {
        return suites.findByOwnerIdAndArchivedFalseOrderByIdAsc(ownership.currentId()).stream()
                .map(this::suiteView)
                .toList();
    }

    public TestSuiteResponse getSuiteById(Long id) {
        return suiteView(suite(id));
    }

    public List<TestPartResponse> getPartsBySuite(Long id) {
        suite(id);
        return parts.findBySuiteIdAndArchivedFalseOrderByToeicPartIdAsc(id).stream()
                .map(this::partView)
                .toList();
    }

    public TestPartResponse getPartById(Long id) {
        return partView(part(id));
    }

    public List<QuestionView> questions(Long partId, boolean solution) {
        part(partId);
        return items.activeItems(partId).stream()
                .map(
                        i -> {
                            var v = views.question(i.getQuestion(), i.getPosition(), solution);
                            v.setPartId(partId);
                            return v;
                        })
                .toList();
    }

    public List<QuestionView> getQuestionsByPart(Long id) {
        return questions(id, false);
    }

    public QuestionView getQuestionById(Long id) {
        var q = content.question(id);
        return views.question(q, q.getPosition(), false);
    }

    @Transactional
    public TestViews.Attempt startTest(String ignored, Long partId) {
        User user = users.requireUser(null);
        userRepository.lockById(user.getId()).orElseThrow();
        String key = user.getId() + ":" + partId;
        var existing = attempts.findByActiveKey(key);
        if (existing.isPresent()) {
            var a = attempts.lockById(existing.get().getId()).orElseThrow();
            if (now().isBefore(a.getExpiresAt())) return view(a, true);
            finish(a, a.getExpiresAt());
            attempts.flush();
        }
        TestPart part = part(partId);
        var members = items.activeItems(partId);
        if (members.isEmpty()) throw new AppException(ErrorCode.INVALID_CONTENT);
        content.validateContext(
                members.stream().map(TestPartItem::getQuestion).toList(),
                part.getAudioUrl() != null && !part.getAudioUrl().isBlank());
        TestAttempt a = new TestAttempt();
        a.setUser(user);
        a.setPart(part);
        a.setActiveKey(key);
        a.setStatus("IN_PROGRESS");
        a.setStartedAt(now());
        a.setTotalCount(members.size());
        a.setExpiresAt(a.getStartedAt().plusMinutes(part.getTimeLimit()));
        a.setPartNumber(part.getToeicPart().getId());
        a.setSuiteName(part.getSuite().getSuiteName());
        a.setAudioUrl(part.getAudioUrl());
        for (var member : members) {
            AttemptItem i = new AttemptItem();
            i.setAttempt(a);
            i.setQuestionId(member.getQuestion().getId());
            i.setPosition(member.getPosition());
            i.setSnapshot(encode(views.question(member.getQuestion(), member.getPosition(), true)));
            a.getItems().add(i);
        }
        return view(attempts.saveAndFlush(a), true);
    }

    @Transactional
    public TestViews.Attempt getTestResult(Long id) {
        TestAttempt a = owned(id);
        if ("IN_PROGRESS".equals(a.getStatus()) && !now().isBefore(a.getExpiresAt()))
            finish(a, a.getExpiresAt());
        return view(a, true);
    }

    @Transactional
    public TestViews.Attempt draft(Long id, List<TestAnswerRequest> answers) {
        TestAttempt a = owned(id);
        requireOwner(a);
        if (!"IN_PROGRESS".equals(a.getStatus()))
            throw new AppException(ErrorCode.TEST_ALREADY_SUBMITTED);
        if (!now().isBefore(a.getExpiresAt())) {
            finish(a, a.getExpiresAt());
            return view(a, true);
        }
        apply(a, answers);
        return view(a, true);
    }

    @Transactional
    public TestViews.Attempt submitTest(String ignored, TestSubmitRequest r) {
        TestAttempt a = owned(r.getResultId());
        requireOwner(a);
        // Idempotent: retrying a successful submission returns the same immutable result.
        if (!"IN_PROGRESS".equals(a.getStatus())) return view(a, true);
        if (now().isBefore(a.getExpiresAt())) apply(a, r.getAnswers());
        finish(a, now().isBefore(a.getExpiresAt()) ? now() : a.getExpiresAt());
        return view(a, true);
    }

    private void requireOwner(TestAttempt a) {
        if (!a.getUser().getId().equals(users.requireUser(null).getId()))
            throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private TestAttempt owned(Long id) {
        TestAttempt a =
                attempts.lockById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.TEST_RESULT_NOT_FOUND));
        var user = users.requireUser(null);
        if (!a.getUser().getId().equals(user.getId()))
            throw new AppException(ErrorCode.UNAUTHORIZED);
        return a;
    }

    private void apply(TestAttempt a, List<TestAnswerRequest> answers) {
        if (answers == null) return;
        Map<Long, AttemptItem> byId = new HashMap<>();
        a.getItems().forEach(i -> byId.put(i.getQuestionId(), i));
        Set<Long> seen = new HashSet<>();
        for (var answer : answers) {
            if (answer == null
                    || !seen.add(answer.getQuestionId())
                    || !byId.containsKey(answer.getQuestionId()))
                throw new AppException(ErrorCode.INVALID_ANSWER);
            AttemptItem item = byId.get(answer.getQuestionId());
            QuestionView snapshot = decode(item);
            String selected = answer.getSelectedAnswer();
            if (selected != null
                    && snapshot.getOptions().stream().noneMatch(o -> o.label().equals(selected)))
                throw new AppException(ErrorCode.INVALID_ANSWER);
            item.setSelectedAnswer(selected);
            item.setAnsweredAt(now());
        }
    }

    private void finish(TestAttempt a, LocalDateTime submitted) {
        int correct = 0;
        for (var i : a.getItems()) {
            boolean ok =
                    i.getSelectedAnswer() != null
                            && i.getSelectedAnswer().equals(decode(i).getCorrectAnswer());
            i.setCorrect(ok);
            if (ok) correct++;
        }
        a.setCorrectCount(correct);
        a.setAccuracyPercent(
                a.getItems().isEmpty() ? 0 : Math.round(correct * 100f / a.getItems().size()));
        a.setTimeSpent(
                (int) Math.max(0, Duration.between(a.getStartedAt(), submitted).getSeconds()));
        a.setSubmittedAt(submitted);
        a.setStatus("SUBMITTED");
        a.setActiveKey(null);
    }

    public HistoryPage getUserTestResults(String ignored, int page, int size) {
        return page(attempts.findByUserId(users.requireUser(null).getId(), paging(page, size)));
    }

    public Map<String, Long> summary() {
        String id = users.requireUser(null).getId();
        return Map.of(
                "started",
                attempts.countByUserId(id),
                "submitted",
                attempts.countByUserIdAndStatus(id, "SUBMITTED"));
    }

    private org.springframework.data.domain.Pageable paging(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new AppException(ErrorCode.INVALID_CONTENT);
        return org.springframework.data.domain.PageRequest.of(
                page,
                size,
                org.springframework.data.domain.Sort.by("startedAt")
                        .descending()
                        .and(org.springframework.data.domain.Sort.by("id").descending()));
    }

    private HistoryPage page(org.springframework.data.domain.Page<TestAttempt> p) {
        return new HistoryPage(
                p.getContent().stream().map(a -> view(a, false)).toList(),
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages());
    }

    @Transactional
    public void expire(Long id) {
        var a = attempts.lockById(id).orElseThrow();
        if ("IN_PROGRESS".equals(a.getStatus()) && !now().isBefore(a.getExpiresAt()))
            finish(a, a.getExpiresAt());
    }

    public HistoryPage allResults(int page, int size) {
        return getUserTestResults(null, page, size);
    }

    public TestViews.Attempt view(TestAttempt a, boolean detail) {
        boolean graded = !"IN_PROGRESS".equals(a.getStatus());
        List<QuestionView> qs = new ArrayList<>();
        List<TestViews.Answer> answers = new ArrayList<>();
        Map<Long, String> selected = new LinkedHashMap<>();
        if (detail)
            for (var i : a.getItems()) {
                QuestionView v = decode(i);
                if (i.getSelectedAnswer() != null)
                    selected.put(i.getQuestionId(), i.getSelectedAnswer());
                if (graded)
                    answers.add(
                            new TestViews.Answer(
                                    i.getId(),
                                    i.getQuestionId(),
                                    i.getPosition(),
                                    v.getQuestionText(),
                                    v.getImageUrl(),
                                    i.getSelectedAnswer(),
                                    v.getCorrectAnswer(),
                                    v.getOptionA(),
                                    v.getOptionB(),
                                    v.getOptionC(),
                                    v.getOptionD(),
                                    v.getExplanation(),
                                    i.getCorrect(),
                                    v.getGroup(),
                                    v.getOptions()));
                qs.add(graded ? v : views.hideSolution(v));
            }
        return new TestViews.Attempt(
                a.getId(),
                a.getPart().getId(),
                a.getPartNumber(),
                a.getSuiteName(),
                iso(a.getStartedAt()),
                iso(a.getExpiresAt()),
                iso(a.getSubmittedAt()),
                a.getTimeSpent(),
                a.getAccuracyPercent(),
                a.getAccuracyPercent(),
                a.getCorrectCount(),
                a.getTotalCount(),
                a.getStatus(),
                a.getAudioUrl(),
                qs,
                selected,
                answers);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private String iso(LocalDateTime t) {
        return t == null ? null : t.atOffset(ZoneOffset.UTC).toString();
    }

    private String encode(QuestionView v) {
        try {
            return json.writeValueAsString(v);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot snapshot question", e);
        }
    }

    private QuestionView decode(AttemptItem i) {
        try {
            return json.readValue(i.getSnapshot(), QuestionView.class);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid stored question snapshot", e);
        }
    }
}
