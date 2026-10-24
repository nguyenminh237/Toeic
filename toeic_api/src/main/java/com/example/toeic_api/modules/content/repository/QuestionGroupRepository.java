package com.example.toeic_api.modules.content.repository;

import com.example.toeic_api.modules.content.model.QuestionGroup;

import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface QuestionGroupRepository extends JpaRepository<QuestionGroup, Long> {
    List<QuestionGroup> findByPartIdAndOwnerIdAndArchivedFalseOrderByIdAsc(
            Integer partId, String ownerId);

    List<QuestionGroup> findByPartIdAndArchivedFalseOrderByIdAsc(Integer partId);
}
