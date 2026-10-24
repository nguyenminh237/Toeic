package com.example.toeic_api.modules.ai.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.*;

public record ExplanationRequest(
        @Min(1) @Max(7) int partNumber,
        @Size(max = 10000) String questionText,
        @Size(max = 1000) String promptText,
        @NotBlank @Pattern(regexp = "[ABCD]") String correctAnswer,
        @NotNull @Size(min = 3, max = 4)
                Map<@Pattern(regexp = "[ABCD]") String, @Size(max = 5000) String> options,
        Long groupId,
        @Size(max = 30000) String transcript,
        @Size(max = 5) List<@Valid Document> documents) {
    public record Document(@Size(max = 300) String title, @Size(max = 30000) String content) {}
}
