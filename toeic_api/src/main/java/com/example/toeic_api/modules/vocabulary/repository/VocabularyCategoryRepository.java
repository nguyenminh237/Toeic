package com.example.toeic_api.modules.vocabulary.repository;

import com.example.toeic_api.modules.vocabulary.model.VocabularyCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VocabularyCategoryRepository extends JpaRepository<VocabularyCategory, Long> {
    java.util.List<VocabularyCategory> findByCreatedByIdAndArchivedFalseOrderByIdAsc(String userId);

    Optional<VocabularyCategory> findByCategoryName(String categoryName);
}
