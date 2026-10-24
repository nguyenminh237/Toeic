package com.example.toeic_api.modules.practice.repository;

import com.example.toeic_api.modules.practice.model.PracticeSet;

import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface PracticeSetRepository extends JpaRepository<PracticeSet, Long> {
    List<PracticeSet> findByPartIdAndOwnerIdAndArchivedFalseOrderByIdAsc(
            Integer partId, String ownerId);

    List<PracticeSet> findByPartIdAndArchivedFalseOrderByIdAsc(Integer partId);
}
