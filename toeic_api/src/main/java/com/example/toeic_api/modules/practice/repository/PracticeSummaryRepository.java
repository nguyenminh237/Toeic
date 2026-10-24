package com.example.toeic_api.modules.practice.repository;

import com.example.toeic_api.modules.practice.dto.response.PracticeViews;

import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PracticeSummaryRepository {
    private final EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<PracticeViews.Summary> summaries(String user, int part) {
        List<Object[]> rows =
                entityManager
                        .createNativeQuery(
                                """
SELECT s.id, COUNT(CASE WHEN g.id IS NOT NULL THEN q.id END) total,
  COALESCE(SUM(CASE WHEN g.id IS NOT NULL AND p.question_version=q.version AND p.group_version=g.version AND p.correct=TRUE THEN 1 ELSE 0 END),0) correct_count,
  COALESCE(SUM(CASE WHEN g.id IS NOT NULL AND p.question_version=q.version AND p.group_version=g.version AND p.correct=FALSE THEN 1 ELSE 0 END),0) wrong_count,
  COALESCE(SUM(CASE WHEN g.id IS NOT NULL AND p.bookmarked=TRUE THEN 1 ELSE 0 END),0) bookmarks
FROM practice_sets s
LEFT JOIN practice_set_items i ON i.set_id=s.id
LEFT JOIN questions q ON q.id=i.question_id AND q.archived=FALSE
LEFT JOIN question_groups g ON g.id=q.group_id AND g.archived=FALSE AND g.owner_id=s.owner_id
LEFT JOIN user_question_progress p ON p.question_id=q.id AND p.user_id=?
WHERE s.part_id=? AND s.archived=FALSE AND s.owner_id=? GROUP BY s.id ORDER BY s.id
""")
                        .setParameter(1, user)
                        .setParameter(2, part)
                        .setParameter(3, user)
                        .getResultList();
        return rows.stream()
                .map(
                        row -> {
                            long total = ((Number) row[1]).longValue(),
                                    correct = ((Number) row[2]).longValue(),
                                    wrong = ((Number) row[3]).longValue();
                            return new PracticeViews.Summary(
                                    ((Number) row[0]).longValue(),
                                    total,
                                    correct,
                                    wrong,
                                    total - correct - wrong,
                                    ((Number) row[4]).longValue());
                        })
                .toList();
    }
}
