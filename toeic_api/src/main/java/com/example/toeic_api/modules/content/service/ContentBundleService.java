package com.example.toeic_api.modules.content.service;

import com.example.toeic_api.modules.content.dto.request.*;
import com.example.toeic_api.modules.content.dto.response.ContentBundleResponse;
import com.example.toeic_api.modules.content.mapper.ContentViews;
import com.example.toeic_api.modules.content.model.Question;
import com.example.toeic_api.modules.practice.service.PracticeAdminService;
import com.example.toeic_api.modules.tests.service.TestAdminService;
import com.example.toeic_api.shared.exception.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ContentBundleService {
    private final ContentService content;
    private final ContentViews views;
    private final PracticeAdminService practice;
    private final TestAdminService tests;
    private final jakarta.persistence.EntityManager entityManager;

    @Transactional
    public List<ContentBundleResponse> saveBatch(ContentBatchRequest request) {
        var ids = new HashSet<Long>();
        int total = 0;
        for (var entry : request.groups()) {
            total += entry.bundle().questions().size();
            if (entry.id() != null && !ids.add(entry.id()))
                throw new AppException(ErrorCode.INVALID_CONTENT);
        }
        if (total > 100)
            throw new AppException(ErrorCode.INVALID_CONTENT, "Mỗi lần lưu tối đa 100 câu.");
        var results = new ArrayList<ContentBundleResponse>();
        for (var entry : request.groups()) results.add(save(entry.id(), entry.bundle()));
        return results;
    }

    @Transactional
    public ContentBundleResponse save(Long groupId, ContentBundleRequest request) {
        var rows = request.questions();
        var positions = new HashSet<Integer>();
        var ids = new HashSet<Long>();
        var existing = groupId == null ? List.<Question>of() : content.questions(groupId);
        if (groupId != null
                && !Objects.equals(request.version(), content.group(groupId).getVersion()))
            throw new AppException(ErrorCode.DATA_CONFLICT);
        for (var row : rows) {
            if (!positions.add(row.position()))
                throw new AppException(ErrorCode.INVALID_CONTENT, "Thứ tự câu trong cụm bị trùng.");
            if (row.id() != null) {
                var old =
                        existing.stream()
                                .filter(q -> q.getId().equals(row.id()))
                                .findFirst()
                                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CONTENT));
                if (!ids.add(row.id()) || !Objects.equals(row.version(), old.getVersion()))
                    throw new AppException(ErrorCode.DATA_CONFLICT);
                // Preserve positions of existing questions; never silently move or overwrite them.
                if (row.position() != old.getPosition())
                    throw new AppException(ErrorCode.INVALID_CONTENT);
            }
        }
        if (!ids.equals(
                existing.stream()
                        .map(Question::getId)
                        .collect(java.util.stream.Collectors.toSet())))
            throw new AppException(ErrorCode.DATA_CONFLICT);
        int part = request.group().partNumber();
        int expected = part == 6 ? 4 : (part == 3 || part == 4 ? 3 : 0);
        if (expected > 0 && rows.size() != expected)
            throw new AppException(
                    ErrorCode.INVALID_CONTENT,
                    "Part " + part + " cần đúng " + expected + " câu trong một cụm.");
        if (part == 6
                && (request.group().documents() == null || request.group().documents().size() != 1))
            throw new AppException(
                    ErrorCode.INVALID_CONTENT, "Part 6 cần một bài đọc dùng chung cho bốn câu.");
        if (part >= 6
                && (request.group().documents() == null
                        || request.group().documents().isEmpty()
                        || request.group().documents().stream()
                                .anyMatch(
                                        d ->
                                                (d.getContent() == null || d.getContent().isBlank())
                                                        && (d.getImageUrl() == null
                                                                || d.getImageUrl().isBlank()))))
            throw new AppException(ErrorCode.INCOMPLETE_CONTEXT);
        var group = content.saveGroup(groupId, request.group());
        var saved = new ArrayList<Question>();
        for (var row : rows)
            saved.add(
                    content.saveQuestion(
                            row.id(),
                            new ContentRequests.Question(
                                    group.getId(),
                                    row.position(),
                                    part == 6
                                                    && (row.questionText() == null
                                                            || row.questionText().isBlank())
                                            ? "Chỗ trống (" + row.position() + ")"
                                            : row.questionText(),
                                    row.promptText(),
                                    row.hideQuestion(),
                                    row.explanation(),
                                    row.options())));
        saved.sort(Comparator.comparing(Question::getPosition));
        content.validateContext(saved);
        if (request.destination() != null) {
            var destination = request.destination();
            Map<Long, Integer> existingPositions = new HashMap<>();
            if (destination.position() == null) {
                var matches =
                        entityManager
                                .createNativeQuery(
                                        "sets".equals(destination.type())
                                                ? "SELECT i.question_id,i.position FROM"
                                                      + " practice_set_items i JOIN questions q ON"
                                                      + " q.id=i.question_id WHERE i.set_id=? AND"
                                                      + " q.group_id=?"
                                                : "SELECT i.question_id,i.position FROM"
                                                      + " test_part_items i JOIN questions q ON"
                                                      + " q.id=i.question_id WHERE i.part_id=? AND"
                                                      + " q.group_id=?")
                                .setParameter(1, destination.id())
                                .setParameter(2, group.getId())
                                .getResultList();
                for (Object match : matches) {
                    Object[] row = (Object[]) match;
                    existingPositions.put(
                            ((Number) row[0]).longValue(), ((Number) row[1]).intValue());
                }
            }
            boolean keepPositions =
                    destination.position() == null
                            && saved.stream()
                                    .allMatch(q -> existingPositions.containsKey(q.getId()));
            int position =
                    destination.position() != null
                            ? destination.position()
                            : ((Number)
                                            entityManager
                                                    .createNativeQuery(
                                                            "sets".equals(destination.type())
                                                                    ? "SELECT"
                                                                          + " COALESCE(MAX(position),0)+1"
                                                                          + " FROM"
                                                                          + " practice_set_items"
                                                                          + " WHERE set_id=?"
                                                                    : "SELECT"
                                                                          + " COALESCE(MAX(position),0)+1"
                                                                          + " FROM test_part_items"
                                                                          + " WHERE part_id=?")
                                                    .setParameter(1, destination.id())
                                                    .getSingleResult())
                                    .intValue();
            for (var question : saved) {
                int itemPosition =
                        keepPositions ? existingPositions.get(question.getId()) : position++;
                if ("sets".equals(destination.type()))
                    practice.attach(destination.id(), question.getId(), itemPosition);
                else tests.attach(destination.id(), question.getId(), itemPosition);
            }
        }
        return new ContentBundleResponse(
                group.getId(),
                group.getVersion(),
                saved.stream().map(q -> views.question(q, q.getPosition(), true)).toList());
    }
}
