package com.example.toeic_api.modules.vocabulary.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public final class VocabularyImportRequests {
    private VocabularyImportRequests() {}

    public record Generate(
            @Min(10) @Max(990) int targetScore,
            @NotEmpty @Size(max = 6) List<@NotBlank @Size(max = 100) String> terms) {}

    public record Confirm(
            @NotNull Long setId,
            @AssertTrue(message = "Hãy duyệt nội dung AI trước khi lưu") boolean reviewed,
            @NotEmpty @Size(max = 30) List<@NotNull @Valid VocabularyWordCreateRequest> words) {}
}
