package com.example.toeic_api.modules.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReadingQuestionRequest {
    @NotNull Long sectionId;
    Long passageId;
    @NotNull Integer questionNumber;
    String questionText;
    @NotBlank String correctAnswer;
    String optionA;
    String optionB;
    String optionC;
    String optionD;
    String explanation;
}
