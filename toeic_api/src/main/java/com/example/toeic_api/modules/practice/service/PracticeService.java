package com.example.toeic_api.modules.practice.service;

import com.example.toeic_api.modules.content.dto.response.QuestionView;
import com.example.toeic_api.modules.content.mapper.ContentViews;
import com.example.toeic_api.modules.content.model.Question;
import com.example.toeic_api.modules.content.model.ToeicPart;
import com.example.toeic_api.modules.content.repository.ToeicPartRepository;
import com.example.toeic_api.modules.content.service.ContentService;
import com.example.toeic_api.modules.practice.dto.response.PracticeViews;
import com.example.toeic_api.modules.practice.model.PracticeAnswerEvent;
import com.example.toeic_api.modules.practice.model.PracticeSet;
import com.example.toeic_api.modules.practice.model.QuestionProgress;
import com.example.toeic_api.modules.practice.repository.PracticeAnswerEventRepository;
import com.example.toeic_api.modules.practice.repository.PracticeSetItemRepository;
import com.example.toeic_api.modules.practice.repository.PracticeSetRepository;
import com.example.toeic_api.modules.practice.repository.PracticeSummaryRepository;
import com.example.toeic_api.modules.practice.repository.QuestionProgressRepository;
import com.example.toeic_api.modules.users.repository.UserRepository;
import com.example.toeic_api.modules.users.service.UserService;
import com.example.toeic_api.shared.exception.AppException;
import com.example.toeic_api.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PracticeService {
    private final ContentService content;
    private final ContentViews views;
    private final ToeicPartRepository parts;
    private final PracticeSetRepository sets;
    private final PracticeSetItemRepository items;
    private final QuestionProgressRepository progress;
    private final PracticeAnswerEventRepository events;
    private final UserService users;
    private final UserRepository userRepository;
    private final com.example.toeic_api.shared.security.ContentOwnership ownership;

    public List<PracticeViews.Part> parts(boolean listening) {
        return parts.findAllByOrderByIdAsc().stream()
                .filter(p -> p.isListening() == listening)
                .map(p -> new PracticeViews.Part(p.getId(), p.getId(), p.getDescription()))
                .toList();
    }

    public PracticeViews.Part part(int n, boolean listening) {
        ToeicPart p = content.part(n);
        check(p, listening);
        return new PracticeViews.Part(n, n, p.getDescription());
    }

    public PracticeSet set(Long id, boolean listening) {
        PracticeSet s =
                sets.findById(id)
                        .filter(x -> !x.isArchived() && ownership.owns(x.getOwnerId()))
                        .orElseThrow(() -> new AppException(ErrorCode.CONTENT_NOT_FOUND));
        check(s.getPart(), listening);
        return s;
    }

    private void check(ToeicPart p, boolean listening) {
        if (p.isListening() != listening) throw new AppException(ErrorCode.INVALID_CONTENT);
    }

    public PracticeViews.Section sectionView(PracticeSet s) {
        return new PracticeViews.Section(
                s.getId(), s.getPart().getId(), s.getName(), s.getDescription(), s.getAudioUrl());
    }

    public List<PracticeViews.Section> sections(int n, boolean listening) {
        part(n, listening);
        return sets
                .findByPartIdAndOwnerIdAndArchivedFalseOrderByIdAsc(n, ownership.currentId())
                .stream()
                .map(this::sectionView)
                .toList();
    }

    public List<QuestionView> questions(Long id, boolean listening, boolean solutions) {
        set(id, listening);
        return items.activeItems(id).stream()
                .map(
                        i -> {
                            var v = views.question(i.getQuestion(), i.getPosition(), solutions);
                            v.setSectionId(id);
                            return v;
                        })
                .toList();
    }

    public QuestionView question(Long id, boolean listening) {
        Question q = content.question(id);
        check(q.getGroup().getPart(), listening);
        return views.question(q, q.getPosition(), false);
    }

    public List<PracticeViews.Passage> passages(Long id) {
        var set = set(id, false);
        return content.listGroups(set.getPart().getId()).stream()
                .filter(g -> !g.getDocuments().isEmpty())
                .map(
                        g ->
                                new PracticeViews.Passage(
                                        g.getId(),
                                        id,
                                        g.getDocuments().getFirst().getTitle(),
                                        g.getDocuments().getFirst().getContent(),
                                        1))
                .toList();
    }

    private PracticeViews.Progress progressView(QuestionProgress p) {
        Question q = p.getQuestion();
        boolean current =
                p.getQuestionVersion() == q.getVersion()
                        && p.getGroupVersion() == q.getGroup().getVersion();
        Boolean correct = current ? p.getCorrect() : null;
        String status = correct == null ? "UNANSWERED" : (correct ? "CORRECT" : "WRONG");
        return new PracticeViews.Progress(
                p.getId(),
                p.getUser().getId(),
                q.getId(),
                current ? p.getSelectedAnswer() : null,
                status,
                p.isBookmarked(),
                p.getAttemptCount(),
                p.getLastAnsweredAt(),
                correct == null ? null : q.correctAnswer(),
                correct == null ? null : q.getExplanation());
    }

    public List<PracticeViews.Progress> progress(boolean listening) {
        return progress
                .forSection(users.requireUser(null).getId(), listening ? 1 : 5, listening ? 4 : 7)
                .stream()
                .map(this::progressView)
                .toList();
    }

    @Transactional
    public PracticeViews.Progress answer(Long questionId, String selected, boolean listening) {
        var user = users.requireUser(null);
        userRepository.lockById(user.getId()).orElseThrow();
        Question q = content.question(questionId);
        check(q.getGroup().getPart(), listening);
        if (selected == null
                || q.getOptions().stream().noneMatch(o -> o.getLabel().equals(selected)))
            throw new AppException(ErrorCode.INVALID_ANSWER);
        QuestionProgress p =
                progress.findByUserIdAndQuestionId(user.getId(), questionId)
                        .orElseGet(
                                () -> {
                                    var x = new QuestionProgress();
                                    x.setUser(user);
                                    x.setQuestion(q);
                                    return x;
                                });
        p.setSelectedAnswer(selected);
        p.setQuestionVersion(q.getVersion());
        p.setGroupVersion(q.getGroup().getVersion());
        p.setCorrect(q.correctAnswer().equals(selected));
        p.setAttemptCount(p.getAttemptCount() + 1);
        p.setLastAnsweredAt(LocalDateTime.now(clock));
        PracticeAnswerEvent e = new PracticeAnswerEvent();
        e.setUserId(user.getId());
        e.setQuestionId(questionId);
        e.setQuestionVersion(q.getVersion());
        e.setGroupVersion(q.getGroup().getVersion());
        e.setSelectedAnswer(selected);
        e.setCorrect(p.getCorrect());
        e.setAnsweredAt(p.getLastAnsweredAt());
        events.save(e);
        return progressView(progress.save(p));
    }

    @Transactional
    public PracticeViews.Progress status(
            Long id, String status, Boolean bookmarked, boolean listening) {
        var user = users.requireUser(null);
        userRepository.lockById(user.getId()).orElseThrow();
        Question q = content.question(id);
        check(q.getGroup().getPart(), listening);
        var p =
                progress.findByUserIdAndQuestionId(user.getId(), id)
                        .orElseGet(
                                () -> {
                                    var x = new QuestionProgress();
                                    x.setUser(user);
                                    x.setQuestion(q);
                                    return x;
                                });
        if (bookmarked != null) p.setBookmarked(bookmarked);
        if ("UNANSWERED".equals(status)) {
            p.setSelectedAnswer(null);
            p.setCorrect(null);
        } else if ("MARKED".equals(status)) p.setBookmarked(true);
        else if (status != null) throw new AppException(ErrorCode.INVALID_PROGRESS_STATUS);
        return progressView(progress.save(p));
    }

    public List<PracticeViews.Summary> summaries(int part, boolean listening) {
        part(part, listening);
        String user = users.requireUser(null).getId();
        // Aggregate in SQL; no question bodies or per-section requests are loaded.
        return summaryRepository.summaries(user, part);
    }

    private final PracticeSummaryRepository summaryRepository;
    private final Clock clock;
}
