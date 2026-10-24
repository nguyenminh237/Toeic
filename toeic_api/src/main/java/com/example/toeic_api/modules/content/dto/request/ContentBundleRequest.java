package com.example.toeic_api.modules.content.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record ContentBundleRequest(
        @NotNull @Valid ContentRequests.Group group,
        Long version,
        @NotEmpty @Size(max = 50) List<@Valid Item> questions,
        @Valid Destination destination) {
    public record Item(
            Long id,
            Long version,
            @Min(1) int position,
            String questionText,
            String promptText,
            boolean hideQuestion,
            String explanation,
            @NotNull @Valid List<ContentRequests.Option> options) {}

    public record Destination(
            @NotBlank @Pattern(regexp = "sets|test-parts") String type,
            @NotNull Long id,
            @Min(1) Integer position) {}
}
