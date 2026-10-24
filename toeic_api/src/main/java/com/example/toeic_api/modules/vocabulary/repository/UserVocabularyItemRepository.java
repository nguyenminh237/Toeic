package com.example.toeic_api.modules.vocabulary.repository;

import com.example.toeic_api.modules.vocabulary.model.UserVocabularyItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserVocabularyItemRepository extends JpaRepository<UserVocabularyItem, Long> {
    Optional<UserVocabularyItem> findByCollectionIdAndWordId(Long collectionId, Long wordId);
}
