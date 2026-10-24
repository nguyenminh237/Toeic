package com.example.toeic_api.modules.vocabulary.mapper;

import com.example.toeic_api.modules.vocabulary.dto.response.VocabularyCategoryResponse;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularySetResponse;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularyWordResponse;
import com.example.toeic_api.modules.vocabulary.model.VocabularyCategory;
import com.example.toeic_api.modules.vocabulary.model.VocabularySet;
import com.example.toeic_api.modules.vocabulary.model.VocabularySynonym;
import com.example.toeic_api.modules.vocabulary.model.VocabularyWord;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface VocabularyMapper {
    VocabularyCategoryResponse toVocabularyCategoryResponse(VocabularyCategory entity);

    @Mapping(source = "category.id", target = "categoryId")
    VocabularySetResponse toVocabularySetResponse(VocabularySet entity);

    @Mapping(target = "setId", ignore = true)
    @Mapping(target = "synonyms", ignore = true)
    @Mapping(target = "senses", ignore = true)
    @Mapping(target = "family", ignore = true)
    VocabularyWordResponse toVocabularyWordResponse(VocabularyWord entity);

    default String map(VocabularySynonym synonym) {
        return synonym.getSynonymPhrase();
    }
}
