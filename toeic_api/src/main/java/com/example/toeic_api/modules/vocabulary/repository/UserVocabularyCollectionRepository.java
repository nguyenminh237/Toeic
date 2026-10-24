package com.example.toeic_api.modules.vocabulary.repository;

import com.example.toeic_api.modules.vocabulary.model.UserVocabularyCollection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserVocabularyCollectionRepository
        extends JpaRepository<UserVocabularyCollection, Long> {
    List<UserVocabularyCollection> findByUserId(String userId);

    Optional<UserVocabularyCollection> findByUserIdAndIsDefaultTrue(String userId);
}
