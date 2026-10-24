package com.example.toeic_api.modules.practice.service;

import com.example.toeic_api.modules.content.dto.request.ContentRequests;
import com.example.toeic_api.modules.content.dto.response.QuestionView;
import com.example.toeic_api.modules.content.mapper.ContentViews;
import com.example.toeic_api.modules.content.model.GroupDocument;
import com.example.toeic_api.modules.content.model.QuestionGroup;
import com.example.toeic_api.modules.content.service.ContentService;
import com.example.toeic_api.modules.practice.dto.request.ListeningPartRequest;
import com.example.toeic_api.modules.practice.dto.request.ListeningQuestionCreateRequest;
import com.example.toeic_api.modules.practice.dto.request.ListeningSectionRequest;
import com.example.toeic_api.modules.practice.dto.request.ReadingPartRequest;
import com.example.toeic_api.modules.practice.dto.request.ReadingPassageRequest;
import com.example.toeic_api.modules.practice.dto.request.ReadingQuestionRequest;
import com.example.toeic_api.modules.practice.dto.request.ReadingSectionRequest;
import com.example.toeic_api.modules.practice.dto.response.PracticeViews;
import com.example.toeic_api.modules.practice.model.PracticeSet;
import com.example.toeic_api.modules.practice.model.PracticeSetItem;
import com.example.toeic_api.modules.practice.repository.PracticeSetItemRepository;
import com.example.toeic_api.modules.practice.repository.PracticeSetRepository;
import com.example.toeic_api.shared.exception.AppException;
import com.example.toeic_api.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PracticeAdminService {
    private final ContentService content;
    private final ContentViews views;
    private final PracticeService practice;
    private final PracticeSetRepository sets;
    private final PracticeSetItemRepository items;
    private final com.example.toeic_api.shared.security.ContentOwnership ownership;
    private final com.example.toeic_api.modules.media.service.PrivateMediaService media;

    private PracticeViews.Part part(int n, String description, boolean listening) {
        var auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .getAuthentication();
        if (auth == null
                || auth.getAuthorities().stream()
                        .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
            throw new AppException(ErrorCode.UNAUTHORIZED);
        if (n < 1 || n > 7 || (n <= 4) != listening)
            throw new AppException(ErrorCode.INVALID_CONTENT);
        var p = content.part(n);
        if (description != null && !description.isBlank()) p.setDescription(description);
        return new PracticeViews.Part(n, n, p.getDescription());
    }

    private PracticeViews.Section section(
            Long id,
            Long partId,
            String name,
            String description,
            String audio,
            boolean listening) {
        if (name == null
                || name.isBlank()
                || name.length() > 255
                || partId == null
                || partId < 1
                || partId > 7) throw new AppException(ErrorCode.INVALID_CONTENT);
        var p = content.part(partId.intValue());
        if (p.isListening() != listening) throw new AppException(ErrorCode.INVALID_CONTENT);
        var s = id == null ? new PracticeSet() : practice.set(id, listening);
        if (id == null) s.setOwnerId(ownership.currentId());
        if (id != null && !s.getPart().getId().equals(p.getId()))
            throw new AppException(ErrorCode.INVALID_CONTENT);
        s.setPart(p);
        media.validateReference(audio);
        s.setDescription(description);
        s.setName(name.trim());
        s.setAudioUrl(audio);
        return practice.sectionView(sets.save(s));
    }

    public PracticeViews.Part createListeningPart(ListeningPartRequest r) {
        return part(r.getPartNumber(), r.getDescription(), true);
    }

    public PracticeViews.Part updateListeningPart(Long id, ListeningPartRequest r) {
        if (id.intValue() != r.getPartNumber()) throw new AppException(ErrorCode.INVALID_CONTENT);
        return createListeningPart(r);
    }

    public void deleteListeningPart(Long id) {
        throw new AppException(ErrorCode.FIXED_PART);
    }

    public PracticeViews.Part createReadingPart(ReadingPartRequest r) {
        return part(r.getPartNumber(), r.getDescription(), false);
    }

    public PracticeViews.Part updateReadingPart(Long id, ReadingPartRequest r) {
        if (id.intValue() != r.getPartNumber()) throw new AppException(ErrorCode.INVALID_CONTENT);
        return createReadingPart(r);
    }

    public void deleteReadingPart(Long id) {
        throw new AppException(ErrorCode.FIXED_PART);
    }

    public PracticeViews.Section createListeningSection(ListeningSectionRequest r) {
        return section(null, r.getPartId(), r.getName(), r.getDescription(), r.getAudioUrl(), true);
    }

    public PracticeViews.Section updateListeningSection(Long id, ListeningSectionRequest r) {
        return section(id, r.getPartId(), r.getName(), r.getDescription(), r.getAudioUrl(), true);
    }

    public void deleteListeningSection(Long id) {
        practice.set(id, true).setArchived(true);
    }

    public PracticeViews.Section createReadingSection(ReadingSectionRequest r) {
        return section(null, r.getPartId(), r.getName(), r.getDescription(), null, false);
    }

    public PracticeViews.Section updateReadingSection(Long id, ReadingSectionRequest r) {
        return section(id, r.getPartId(), r.getName(), r.getDescription(), null, false);
    }

    public void deleteReadingSection(Long id) {
        practice.set(id, false).setArchived(true);
    }

    public QuestionView createListeningQuestion(ListeningQuestionCreateRequest r) {
        return listening(null, r);
    }

    public QuestionView updateListeningQuestion(Long id, ListeningQuestionCreateRequest r) {
        return listening(id, r);
    }

    private QuestionView listening(Long id, ListeningQuestionCreateRequest r) {
        var s = practice.set(r.getSectionId(), true);
        Long groupId = r.getGroupId();
        if (id != null && groupId == null) groupId = content.question(id).getGroup().getId();
        if (groupId == null)
            groupId =
                    content.saveGroup(
                                    null,
                                    new ContentRequests.Group(
                                            s.getPart().getId(),
                                            null,
                                            r.getAudioUrl(),
                                            r.getImageUrl(),
                                            null,
                                            null,
                                            null,
                                            List.of()))
                            .getId();
        else {
            var g = content.group(groupId);
            if (!g.getPart().getId().equals(s.getPart().getId()))
                throw new AppException(ErrorCode.INVALID_CONTENT);
            content.saveGroup(
                    groupId,
                    new ContentRequests.Group(
                            g.getPart().getId(),
                            g.getTitle(),
                            r.getAudioUrl(),
                            r.getImageUrl(),
                            g.getAudioStartMs(),
                            g.getAudioEndMs(),
                            g.getTranscript(),
                            new ArrayList<>(g.getDocuments())));
        }
        var q =
                content.saveQuestion(
                        id,
                        new ContentRequests.Question(
                                groupId,
                                r.getQuestionNumber(),
                                r.getQuestionText(),
                                r.getPromptText(),
                                Boolean.TRUE.equals(r.getHideQuestion()),
                                r.getExplanation(),
                                options(
                                        s.getPart().getId(),
                                        r.getCorrectAnswer(),
                                        r.getOptionA(),
                                        r.getOptionB(),
                                        r.getOptionC(),
                                        r.getOptionD())));
        attach(s.getId(), q.getId(), r.getQuestionNumber());
        var v = views.question(q, r.getQuestionNumber(), true);
        v.setSectionId(s.getId());
        return v;
    }

    public QuestionView createReadingQuestion(ReadingQuestionRequest r) {
        return reading(null, r);
    }

    public QuestionView updateReadingQuestion(Long id, ReadingQuestionRequest r) {
        return reading(id, r);
    }

    private QuestionView reading(Long id, ReadingQuestionRequest r) {
        var s = practice.set(r.getSectionId(), false);
        Long groupId = r.getPassageId();
        if (groupId == null && id != null) groupId = content.question(id).getGroup().getId();
        if (groupId == null)
            groupId =
                    content.saveGroup(
                                    null,
                                    new ContentRequests.Group(
                                            s.getPart().getId(),
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            List.of()))
                            .getId();
        if (!content.group(groupId).getPart().getId().equals(s.getPart().getId()))
            throw new AppException(ErrorCode.INVALID_CONTENT);
        var q =
                content.saveQuestion(
                        id,
                        new ContentRequests.Question(
                                groupId,
                                r.getQuestionNumber(),
                                r.getQuestionText(),
                                null,
                                false,
                                r.getExplanation(),
                                options(
                                        s.getPart().getId(),
                                        r.getCorrectAnswer(),
                                        r.getOptionA(),
                                        r.getOptionB(),
                                        r.getOptionC(),
                                        r.getOptionD())));
        attach(s.getId(), q.getId(), r.getQuestionNumber());
        var v = views.question(q, r.getQuestionNumber(), true);
        v.setSectionId(s.getId());
        return v;
    }

    public static List<ContentRequests.Option> options(int part, String correct, String... text) {
        if (correct == null || !correct.matches(part == 2 ? "[A-C]" : "[A-D]"))
            throw new AppException(ErrorCode.INVALID_ANSWER);
        var out = new ArrayList<ContentRequests.Option>();
        for (int n = 0; n < (part == 2 ? 3 : 4); n++) {
            String label = String.valueOf((char) ('A' + n));
            out.add(new ContentRequests.Option(label, text[n], label.equals(correct)));
        }
        return out;
    }

    public void deleteListeningQuestion(Long id) {
        content.archiveQuestion(id);
    }

    public void deleteReadingQuestion(Long id) {
        content.archiveQuestion(id);
    }

    public void attach(Long setId, Long questionId, int position) {
        var s =
                sets.findById(setId)
                        .filter(x -> !x.isArchived() && ownership.owns(x.getOwnerId()))
                        .orElseThrow(() -> new AppException(ErrorCode.CONTENT_NOT_FOUND));
        var q = content.question(questionId);
        if (!q.getGroup().getPart().getId().equals(s.getPart().getId()) || position < 1)
            throw new AppException(ErrorCode.INVALID_CONTENT);
        var i = items.findBySetIdAndQuestionId(setId, questionId).orElseGet(PracticeSetItem::new);
        i.setSet(s);
        i.setQuestion(q);
        i.setPosition(position);
        items.save(i);
    }

    public void detach(Long setId, Long questionId) {
        ownership.require(
                sets.findById(setId)
                        .orElseThrow(() -> new AppException(ErrorCode.CONTENT_NOT_FOUND))
                        .getOwnerId());
        content.question(questionId);
        items.deleteBySetIdAndQuestionId(setId, questionId);
    }

    public List<PracticeViews.Passage> getPassagesBySection(Long sectionId) {
        return practice.passages(sectionId);
    }

    public PracticeViews.Passage createPassage(ReadingPassageRequest r) {
        return passage(null, r);
    }

    public PracticeViews.Passage updatePassage(Long id, ReadingPassageRequest r) {
        return passage(id, r);
    }

    private PracticeViews.Passage passage(Long id, ReadingPassageRequest r) {
        var set = practice.set(r.getSectionId(), false);
        QuestionGroup previous = id == null ? null : content.group(id);
        if (previous != null && previous.getDocuments().size() > 1)
            throw new AppException(ErrorCode.USE_CONTENT_BANK);
        String image =
                previous == null || previous.getDocuments().isEmpty()
                        ? null
                        : previous.getDocuments().getFirst().getImageUrl();
        var g =
                content.saveGroup(
                        id,
                        new ContentRequests.Group(
                                set.getPart().getId(),
                                r.getPassageTitle(),
                                previous == null ? null : previous.getAudioUrl(),
                                previous == null ? null : previous.getImageUrl(),
                                previous == null ? null : previous.getAudioStartMs(),
                                previous == null ? null : previous.getAudioEndMs(),
                                previous == null ? null : previous.getTranscript(),
                                List.of(
                                        new GroupDocument(
                                                r.getPassageTitle(),
                                                r.getPassageContent(),
                                                image))));
        return new PracticeViews.Passage(
                g.getId(),
                set.getId(),
                r.getPassageTitle(),
                r.getPassageContent(),
                r.getPassageOrder());
    }

    public void deletePassage(Long id) {
        content.archiveGroup(id);
    }
}
