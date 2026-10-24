package com.example.toeic_api.modules.tests.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TestSuiteRequest {
    @NotBlank String suiteName;
    String description;
    String source;
}
