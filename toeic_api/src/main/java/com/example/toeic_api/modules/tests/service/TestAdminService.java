package com.example.toeic_api.modules.tests.service;

import com.example.toeic_api.modules.content.dto.request.ContentRequests;
import com.example.toeic_api.modules.content.dto.response.QuestionView;
import com.example.toeic_api.modules.content.mapper.ContentViews;
import com.example.toeic_api.modules.content.service.ContentService;
import com.example.toeic_api.modules.practice.service.PracticeAdminService;
import com.example.toeic_api.modules.tests.dto.request.TestPartRequest;
import com.example.toeic_api.modules.tests.dto.request.TestQuestionRequest;
import com.example.toeic_api.modules.tests.dto.request.TestSuiteRequest;
import com.example.toeic_api.modules.tests.dto.response.HistoryPage;
import com.example.toeic_api.modules.tests.dto.response.TestPartResponse;
import com.example.toeic_api.modules.tests.dto.response.TestSuiteResponse;
import com.example.toeic_api.modules.tests.model.TestPart;
import com.example.toeic_api.modules.tests.model.TestPartItem;
import com.example.toeic_api.modules.tests.model.TestSuite;
import com.example.toeic_api.modules.tests.repository.TestPartItemRepository;
import com.example.toeic_api.modules.tests.repository.TestPartRepository;
import com.example.toeic_api.modules.tests.repository.TestSuiteRepository;
import com.example.toeic_api.shared.exception.AppException;
import com.example.toeic_api.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TestAdminService {
    private final TestService tests;
    private final ContentService content;
    private final ContentViews views;
    private final TestSuiteRepository suites;
    private final TestPartRepository parts;
    private final TestPartItemRepository items;
    private final com.example.toeic_api.shared.security.ContentOwnership ownership;
    private final com.example.toeic_api.modules.media.service.PrivateMediaService media;

    public TestSuiteResponse createSuite(TestSuiteRequest r) {
        return suite(null, r);
    }

    public TestSuiteResponse updateSuite(Long id, TestSuiteRequest r) {
        return suite(id, r);
    }

    private TestSuiteResponse suite(Long id, TestSuiteRequest r) {
        var s = id == null ? new TestSuite() : tests.suite(id);
        if (id == null) s.setOwnerId(ownership.currentId());
        s.setSuiteName(r.getSuiteName());
        s.setDescription(r.getDescription());
        s.setSource(r.getSource());
        return tests.suiteView(suites.save(s));
    }

    public void deleteSuite(Long id) {
        tests.suite(id).setArchived(true);
    }

    public TestPartResponse createTestPart(TestPartRequest r) {
        return part(null, r);
    }

    public TestPartResponse updateTestPart(Long id, TestPartRequest r) {
        return part(id, r);
    }

    private TestPartResponse part(Long id, TestPartRequest r) {
        if (r.getTimeLimit() == null || r.getTimeLimit() <= 0)
            throw new AppException(ErrorCode.INVALID_CONTENT);
        var p = id == null ? new TestPart() : tests.part(id);
        var toeic = content.part(r.getPartNumber());
        if (id != null && !p.getToeicPart().getId().equals(toeic.getId()))
            throw new AppException(ErrorCode.INVALID_CONTENT);
        p.setSuite(tests.suite(r.getSuiteId()));
        p.setToeicPart(toeic);
        p.setTimeLimit(r.getTimeLimit());
        p.setAudioUrl(r.getAudioUrl());
        media.validateReference(r.getAudioUrl());
        return tests.partView(parts.saveAndFlush(p));
    }

    public void deleteTestPart(Long id) {
        tests.part(id).setArchived(true);
    }

    public QuestionView createTestQuestion(TestQuestionRequest r) {
        return question(null, r);
    }

    public QuestionView updateTestQuestion(Long id, TestQuestionRequest r) {
        return question(id, r);
    }

    private QuestionView question(Long id, TestQuestionRequest r) {
        var part = tests.part(r.getPartId());
        Long groupId = r.getGroupId() != null ? r.getGroupId() : r.getPassageId();
        if (groupId == null && id != null) groupId = content.question(id).getGroup().getId();
        if (groupId == null)
            groupId =
                    content.saveGroup(
                                    null,
                                    new ContentRequests.Group(
                                            part.getToeicPart().getId(),
                                            null,
                                            r.getAudioUrl(),
                                            r.getImageUrl(),
                                            null,
                                            null,
                                            null,
                                            List.of()))
                            .getId();
        if (!content.group(groupId).getPart().getId().equals(part.getToeicPart().getId()))
            throw new AppException(ErrorCode.INVALID_CONTENT);
        if (id != null) {
            var g = content.group(groupId);
            content.saveGroup(
                    groupId,
                    new ContentRequests.Group(
                            g.getPart().getId(),
                            g.getTitle(),
                            r.getAudioUrl() == null ? g.getAudioUrl() : r.getAudioUrl(),
                            r.getImageUrl() == null ? g.getImageUrl() : r.getImageUrl(),
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
                                null,
                                part.getToeicPart().getId() == 2,
                                r.getExplanation(),
                                PracticeAdminService.options(
                                        part.getToeicPart().getId(),
                                        r.getCorrectAnswer(),
                                        r.getOptionA(),
                                        r.getOptionB(),
                                        r.getOptionC(),
                                        r.getOptionD())));
        attach(part.getId(), q.getId(), r.getQuestionNumber());
        var v = views.question(q, r.getQuestionNumber(), true);
        v.setPartId(part.getId());
        return v;
    }

    public void attach(Long partId, Long questionId, int position) {
        var p = tests.part(partId);
        var q = content.question(questionId);
        if (!p.getToeicPart().getId().equals(q.getGroup().getPart().getId()) || position < 1)
            throw new AppException(ErrorCode.INVALID_CONTENT);
        var i = items.findByPartIdAndQuestionId(partId, questionId).orElseGet(TestPartItem::new);
        i.setPart(p);
        i.setQuestion(q);
        i.setPosition(position);
        items.save(i);
    }

    public void detach(Long partId, Long questionId) {
        tests.part(partId);
        content.question(questionId);
        items.deleteByPartIdAndQuestionId(partId, questionId);
    }

    public void deleteTestQuestion(Long id) {
        content.archiveQuestion(id);
    }

    public HistoryPage getAllResults(int page, int size) {
        return tests.allResults(page, size);
    }
}
