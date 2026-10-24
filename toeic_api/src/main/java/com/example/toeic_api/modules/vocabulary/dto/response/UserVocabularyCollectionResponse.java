package com.example.toeic_api.modules.vocabulary.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserVocabularyCollectionResponse {
    Long id;
    String setName;
    Boolean isDefault;
    LocalDateTime createdAt;
    Set<VocabularyWordResponse> items;
}
