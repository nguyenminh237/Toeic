package com.example.toeic_api.modules.tests.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TestAnswerRequest {
    Long questionId;
    String selectedAnswer;
}
