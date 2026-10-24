package com.example.toeic_api.modules.content.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record ContentBatchRequest(@NotEmpty @Size(max = 20) List<@Valid Entry> groups) {
    public record Entry(Long id, @NotNull @Valid ContentBundleRequest bundle) {}
}
