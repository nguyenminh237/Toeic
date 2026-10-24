package com.example.toeic_api.modules.practice.repository;

import com.example.toeic_api.modules.practice.model.PracticeSetItem;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface PracticeSetItemRepository extends JpaRepository<PracticeSetItem, Long> {
    @Query(
            "select i from PracticeSetItem i join fetch i.question q join fetch q.group g where"
                + " i.set.id=:id and q.archived=false and g.archived=false and"
                + " g.ownerId=i.set.ownerId order by i.position")
    List<PracticeSetItem> activeItems(@Param("id") Long id);

    Optional<PracticeSetItem> findBySetIdAndQuestionId(Long setId, Long questionId);

    void deleteBySetIdAndQuestionId(Long setId, Long questionId);
}
