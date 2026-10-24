package com.example.toeic_api.modules.tests.dto.response;

import com.example.toeic_api.modules.content.dto.response.QuestionView;

import java.util.*;

public class TestViews {
    public record Attempt(
            Long id,
            Long partId,
            int partNumber,
            String suiteName,
            String startedAt,
            String expiresAt,
            String submittedAt,
            Integer timeSpent,
            Integer score,
            Integer accuracyPercent,
            Integer correctCount,
            int totalCount,
            String status,
            String audioUrl,
            List<QuestionView> questions,
            Map<Long, String> selectedAnswers,
            List<Answer> answers) {}

    public record Answer(
            Long id,
            Long questionId,
            int questionNumber,
            String questionText,
            String imageUrl,
            String selectedAnswer,
            String correctAnswer,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String explanation,
            Boolean isCorrect,
            QuestionView.Group group,
            List<QuestionView.Option> options) {}
}
