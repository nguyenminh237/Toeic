package com.example.toeic_api.modules.vocabulary.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VocabularyCategoryResponse {
    Long id;
    String categoryName;
    String description;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
