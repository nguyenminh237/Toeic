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
public class TestQuestionRequest {
    @NotNull Long partId;
    Long groupId;
    @NotNull Integer questionNumber;
    String audioUrl;
    String imageUrl;
    Long passageId;
    String questionText;
    @NotBlank String correctAnswer;
    String optionA;
    String optionB;
    String optionC;
    String optionD;
    String explanation;
}
