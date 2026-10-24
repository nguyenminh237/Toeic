package com.example.toeic_api.modules.content.dto.request;

import com.example.toeic_api.modules.content.model.GroupDocument;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public class ContentRequests {
    public record Group(
            @NotNull @Min(1) @Max(7) Integer partNumber,
            String title,
            String audioUrl,
            String imageUrl,
            @Min(0) Integer audioStartMs,
            @Min(1) Integer audioEndMs,
            String transcript,
            @Valid List<GroupDocument> documents) {}

    public record Option(
            @NotBlank @Pattern(regexp = "[A-D]") String label, String content, boolean correct) {}

    public record Question(
            @NotNull Long groupId,
            @Min(1) int position,
            String questionText,
            String promptText,
            boolean hideQuestion,
            String explanation,
            @NotNull @Valid List<Option> options) {}

    public record Membership(@NotNull Long questionId, @Min(1) int position) {}

    public record GroupMembership(@Min(1) int position) {}
}
