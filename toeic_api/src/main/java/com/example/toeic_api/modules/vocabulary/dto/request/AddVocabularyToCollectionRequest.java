package com.example.toeic_api.modules.vocabulary.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddVocabularyToCollectionRequest {
    Long wordId;
    Long collectionId;
}
