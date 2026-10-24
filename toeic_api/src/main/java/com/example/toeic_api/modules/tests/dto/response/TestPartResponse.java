package com.example.toeic_api.modules.tests.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TestPartResponse {
    Long id;
    Long suiteId;
    Integer partNumber;
    String partType;
    Integer questionCount;
    Integer timeLimit;
    String audioUrl;
    LocalDateTime createdAt;
}
