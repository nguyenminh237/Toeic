package com.example.toeic_api.modules.tests.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TestSubmitRequest {
    @NotNull Long resultId;

    @Valid @Builder.Default List<TestAnswerRequest> answers = new ArrayList<>();
}
