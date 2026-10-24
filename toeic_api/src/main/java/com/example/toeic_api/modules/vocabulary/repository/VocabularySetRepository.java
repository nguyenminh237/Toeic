package com.example.toeic_api.modules.vocabulary.repository;

import com.example.toeic_api.modules.vocabulary.model.VocabularySet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularySetRepository extends JpaRepository<VocabularySet, Long> {
    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select s from VocabularySet s where s.id=:id")
    java.util.Optional<VocabularySet> lockForImport(
            @org.springframework.data.repository.query.Param("id") Long id);

    List<VocabularySet> findByCategoryId(Long categoryId);
}
