package com.example.toeic_api.modules.content.service;

import com.example.toeic_api.modules.content.dto.request.ContentRequests;
import com.example.toeic_api.modules.content.model.Question;
import com.example.toeic_api.modules.content.model.QuestionGroup;
import com.example.toeic_api.modules.content.model.QuestionOption;
import com.example.toeic_api.modules.content.model.ToeicPart;
import com.example.toeic_api.modules.content.repository.QuestionGroupRepository;
import com.example.toeic_api.modules.content.repository.QuestionRepository;
import com.example.toeic_api.modules.content.repository.ToeicPartRepository;
import com.example.toeic_api.shared.exception.AppException;
import com.example.toeic_api.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentService {
    private final ToeicPartRepository parts;
    private final QuestionGroupRepository groups;
    private final QuestionRepository questions;
    private final com.example.toeic_api.shared.security.ContentOwnership ownership;
    private final com.example.toeic_api.modules.media.service.PrivateMediaService media;

    public ToeicPart part(int id) {
        return parts.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_CONTENT));
    }

    public QuestionGroup group(Long id) {
        return groups.findById(id)
                .filter(g -> !g.isArchived() && ownership.owns(g.getOwnerId()))
                .orElseThrow(() -> new AppException(ErrorCode.CONTENT_NOT_FOUND));
    }

    public Question question(Long id) {
        return questions
                .findById(id)
                .filter(
                        q ->
                                !q.isArchived()
                                        && !q.getGroup().isArchived()
                                        && ownership.owns(q.getGroup().getOwnerId()))
                .orElseThrow(() -> new AppException(ErrorCode.CONTENT_NOT_FOUND));
    }

    public List<QuestionGroup> listGroups(int part) {
        return groups.findByPartIdAndOwnerIdAndArchivedFalseOrderByIdAsc(
                part, ownership.currentId());
    }

    public List<Question> questions(Long groupId) {
        group(groupId);
        return questions.findByGroupIdAndArchivedFalseOrderByPositionAsc(groupId);
    }

    public QuestionGroup saveGroup(Long id, ContentRequests.Group r) {
        QuestionGroup g = id == null ? new QuestionGroup() : group(id);
        if (id == null) g.setOwnerId(ownership.currentId());
        media.validateReference(r.audioUrl());
        media.validateReference(r.imageUrl());
        if (r.documents() != null)
            r.documents().forEach(d -> media.validateReference(d.getImageUrl()));
        if (id != null && g.getPart().getId() != r.partNumber().intValue())
            throw new AppException(ErrorCode.INVALID_CONTENT);
        if ((r.audioStartMs() == null) != (r.audioEndMs() == null)
                || (r.audioStartMs() != null && r.audioEndMs() <= r.audioStartMs()))
            throw new AppException(ErrorCode.INVALID_CONTENT);
        g.setPart(part(r.partNumber()));
        g.setTitle(r.title());
        g.setAudioUrl(r.audioUrl());
        g.setImageUrl(r.imageUrl());
        g.setAudioStartMs(r.audioStartMs());
        g.setAudioEndMs(r.audioEndMs());
        g.setTranscript(r.transcript());
        g.getDocuments().clear();
        if (r.documents() != null) g.getDocuments().addAll(r.documents());
        return groups.saveAndFlush(g);
    }

    public Question saveQuestion(Long id, ContentRequests.Question r) {
        QuestionGroup g = group(r.groupId());
        validateOptions(g.getPart(), r.options());
        Question q = id == null ? new Question() : question(id);
        if (id != null && !q.getGroup().getPart().getId().equals(g.getPart().getId()))
            throw new AppException(ErrorCode.INVALID_CONTENT);
        q.setGroup(g);
        q.setPosition(r.position());
        q.setQuestionText(r.questionText());
        q.setPromptText(r.promptText());
        q.setExplanation(r.explanation());
        q.setHideQuestion(r.hideQuestion());
        q.getOptions().clear();
        r.options().stream()
                .sorted(Comparator.comparing(ContentRequests.Option::label))
                .forEach(
                        o ->
                                q.getOptions()
                                        .add(
                                                new QuestionOption(
                                                        o.label(), o.content(), o.correct())));
        return questions.saveAndFlush(q);
    }

    public void validateOptions(ToeicPart part, List<ContentRequests.Option> opts) {
        List<String> expected = List.of("A", "B", "C", "D").subList(0, part.optionCount());
        if (opts == null
                || opts.size() != expected.size()
                || !opts.stream()
                        .map(ContentRequests.Option::label)
                        .sorted()
                        .toList()
                        .equals(expected)
                || opts.stream().filter(ContentRequests.Option::correct).count() != 1)
            throw new AppException(ErrorCode.INVALID_CONTENT);
    }

    public void archiveQuestion(Long id) {
        question(id).setArchived(true);
    }

    public void archiveGroup(Long id) {
        group(id).setArchived(true);
    }

    public void validateContext(Collection<Question> qs) {
        validateContext(qs, false);
    }

    public void validateContext(Collection<Question> qs, boolean sharedAudio) {
        for (var q : qs) {
            int part = q.getGroup().getPart().getId();
            if (part >= 3
                    && q.getOptions().stream()
                            .anyMatch(o -> o.getContent() == null || o.getContent().isBlank()))
                throw new AppException(ErrorCode.INVALID_CONTENT);
            if ((part == 3 || part == 4 || part == 5 || part == 7)
                    && (q.getQuestionText() == null || q.getQuestionText().isBlank()))
                throw new AppException(ErrorCode.INVALID_CONTENT);
            if (part <= 4
                    && !sharedAudio
                    && (q.getGroup().getAudioUrl() == null || q.getGroup().getAudioUrl().isBlank()))
                throw new AppException(ErrorCode.INCOMPLETE_CONTEXT);
            if (part == 1
                    && (q.getGroup().getImageUrl() == null || q.getGroup().getImageUrl().isBlank()))
                throw new AppException(ErrorCode.INCOMPLETE_CONTEXT);
        }
        Set<Long> closed = new HashSet<>();
        Long previous = null;
        for (Question q : qs) {
            Long current = q.getGroup().getId();
            if (!current.equals(previous)) {
                if (closed.contains(current)) throw new AppException(ErrorCode.INCOMPLETE_CONTEXT);
                if (previous != null) closed.add(previous);
                previous = current;
            }
        }
        Map<Long, List<Question>> byGroup =
                qs.stream()
                        .collect(java.util.stream.Collectors.groupingBy(q -> q.getGroup().getId()));
        for (var entry : byGroup.entrySet()) {
            QuestionGroup g = entry.getValue().getFirst().getGroup();
            int p = g.getPart().getId();
            if (p == 3 || p == 4 || p == 6 || p == 7) {
                List<Long> all =
                        questions
                                .findByGroupIdAndArchivedFalseOrderByPositionAsc(g.getId())
                                .stream()
                                .map(Question::getId)
                                .sorted()
                                .toList();
                if (!all.equals(entry.getValue().stream().map(Question::getId).sorted().toList()))
                    throw new AppException(ErrorCode.INCOMPLETE_CONTEXT);
                if ((p == 3 || p == 4)
                        && (all.size() != 3
                                || (!sharedAudio
                                        && (g.getAudioUrl() == null || g.getAudioUrl().isBlank()))))
                    throw new AppException(ErrorCode.INCOMPLETE_CONTEXT);
                if (p == 6 && all.size() != 4) throw new AppException(ErrorCode.INCOMPLETE_CONTEXT);
                if ((p == 6 || p == 7) && g.getDocuments().isEmpty())
                    throw new AppException(ErrorCode.INCOMPLETE_CONTEXT);
            }
        }
    }
}
