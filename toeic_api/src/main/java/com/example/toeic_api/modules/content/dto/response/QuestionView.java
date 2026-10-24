package com.example.toeic_api.modules.content.dto.response;

import com.example.toeic_api.modules.content.model.GroupDocument;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

import java.util.*;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionView {
    private Long id;
    private Long sectionId;
    private Long passageId;
    private Long partId;
    private int partNumber;
    private int questionNumber;
    private long version;
    private String questionText, promptText, correctAnswer, explanation;
    private boolean hideQuestion;
    private String audioUrl, imageUrl;
    private Integer audioStartMs, audioEndMs;
    private String optionA, optionB, optionC, optionD;
    private List<Option> options = new ArrayList<>();
    private Group group;

    public record Option(String label, String content) {}

    public record Group(
            Long id,
            String title,
            String audioUrl,
            String imageUrl,
            Integer audioStartMs,
            Integer audioEndMs,
            String transcript,
            List<GroupDocument> documents) {}
}
