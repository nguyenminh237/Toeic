package com.example.toeic_api.modules.vocabulary.repository;

import com.example.toeic_api.modules.vocabulary.model.VocabularySynonym;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularySynonymRepository extends JpaRepository<VocabularySynonym, Long> {
    List<VocabularySynonym> findByWordId(Long wordId);
}
