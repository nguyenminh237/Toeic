package com.example.toeic_api.modules.practice.dto.response;

import java.time.LocalDateTime;

public class PracticeViews {
    public record Part(Integer id, Integer partNumber, String description) {}

    public record Section(
            Long id, Integer partId, String name, String description, String audioUrl) {}

    public record Progress(
            Long id,
            String userId,
            Long questionId,
            String selectedAnswer,
            String status,
            boolean bookmarked,
            long attemptCount,
            LocalDateTime lastAttempted,
            String correctAnswer,
            String explanation) {}

    public record Summary(
            Long sectionId,
            long total,
            long correct,
            long wrong,
            long unanswered,
            long bookmarked) {}

    public record Passage(
            Long id,
            Long sectionId,
            String passageTitle,
            String passageContent,
            Integer passageOrder) {}
}
