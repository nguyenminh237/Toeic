package com.example.toeic_api.modules.vocabulary.repository;

import com.example.toeic_api.modules.vocabulary.model.UserVocabularyProgress;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserVocabularyProgressRepository
        extends JpaRepository<UserVocabularyProgress, Long> {
    Optional<UserVocabularyProgress> findByUserIdAndWordId(String userId, Long wordId);
}
