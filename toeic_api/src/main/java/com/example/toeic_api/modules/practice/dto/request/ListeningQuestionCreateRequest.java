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
public class ListeningQuestionCreateRequest {
    @NotNull(message = "SECTION_ID_REQUIRED")
    Long sectionId;

    Long groupId;

    @NotNull(message = "QUESTION_NUMBER_REQUIRED")
    Integer questionNumber;

    String audioUrl;

    String imageUrl;

    String promptText;

    String questionText;

    Boolean hideQuestion;

    @NotBlank(message = "CORRECT_ANSWER_REQUIRED")
    String correctAnswer;

    String optionA;
    String optionB;
    String optionC;
    String optionD;

    String explanation;
}
