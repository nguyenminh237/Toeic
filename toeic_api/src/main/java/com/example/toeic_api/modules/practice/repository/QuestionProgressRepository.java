package com.example.toeic_api.modules.practice.repository;

import com.example.toeic_api.modules.practice.model.QuestionProgress;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface QuestionProgressRepository extends JpaRepository<QuestionProgress, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<QuestionProgress> findByUserIdAndQuestionId(String userId, Long questionId);

    @Query(
            "select p from QuestionProgress p join fetch p.question q join fetch q.group g where"
                + " p.user.id=:user and g.part.id between :from and :to and q.archived=false and"
                + " g.archived=false and g.ownerId=:user")
    List<QuestionProgress> forSection(
            @Param("user") String user, @Param("from") int from, @Param("to") int to);
}
