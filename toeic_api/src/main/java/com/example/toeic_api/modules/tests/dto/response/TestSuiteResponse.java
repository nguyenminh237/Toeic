package com.example.toeic_api.modules.tests.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TestSuiteResponse {
    Long id;
    String suiteName;
    String description;
    String source;
    LocalDateTime createdAt;
}
