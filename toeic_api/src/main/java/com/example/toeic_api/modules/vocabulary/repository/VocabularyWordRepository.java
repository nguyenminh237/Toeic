package com.example.toeic_api.modules.vocabulary.repository;

import com.example.toeic_api.modules.vocabulary.model.VocabularyWord;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface VocabularyWordRepository extends JpaRepository<VocabularyWord, Long> {
    @EntityGraph(attributePaths = "synonyms")
    @Query(
            "select distinct w from VocabularyWord w join w.sets s where s.id=:id and"
                + " w.ownerId=:owner and w.archived=false order by w.id")
    List<VocabularyWord> findBySetIdAndOwner(@Param("id") Long id, @Param("owner") String owner);

    @Query(
            "select lower(trim(w.word)) from VocabularyWord w join w.sets s where s.id=:id and"
                    + " w.archived=false and w.ownerId=s.category.createdBy.id")
    List<String> activeTermsBySet(@Param("id") Long id);

    @EntityGraph(attributePaths = "synonyms")
    @Query(
            "select distinct w from VocabularyWord w join w.sets s where s.id=:id and"
                + " w.archived=false and s.archived=false and s.category.archived=false order by"
                + " w.id")
    List<VocabularyWord> findBySetId(@Param("id") Long id);
}
