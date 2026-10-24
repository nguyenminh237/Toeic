package com.example.toeic_api.modules.content.repository;

import com.example.toeic_api.modules.content.model.Question;

import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByGroupIdAndArchivedFalseOrderByPositionAsc(Long groupId);

    long countByArchivedFalse();
}
