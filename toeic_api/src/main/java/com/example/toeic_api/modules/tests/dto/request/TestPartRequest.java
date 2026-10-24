package com.example.toeic_api.modules.tests.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TestPartRequest {
    @NotNull Long suiteId;
    @NotNull Integer partNumber;
    @NotBlank String partType;
    @NotNull Integer questionCount;
    @NotNull Integer timeLimit;

    String audioUrl;
}
