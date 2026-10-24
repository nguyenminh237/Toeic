package com.example.toeic_api.modules.tests.repository;

import com.example.toeic_api.modules.tests.model.TestPartItem;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface TestPartItemRepository extends JpaRepository<TestPartItem, Long> {
    @Query(
            "select i from TestPartItem i join fetch i.question q join fetch q.group g where"
                + " i.part.id=:id and q.archived=false and g.archived=false and"
                + " g.ownerId=i.part.suite.ownerId order by i.position")
    List<TestPartItem> activeItems(@Param("id") Long id);

    @Query(
            "select count(i) from TestPartItem i where i.part.id=:id and i.question.archived=false"
                + " and i.question.group.archived=false and"
                + " i.question.group.ownerId=i.part.suite.ownerId")
    long countActive(@Param("id") Long id);

    Optional<TestPartItem> findByPartIdAndQuestionId(Long partId, Long questionId);

    void deleteByPartIdAndQuestionId(Long partId, Long questionId);
}
