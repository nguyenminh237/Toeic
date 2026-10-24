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
public class ListeningSectionRequest {
    @NotNull Long partId;

    @NotBlank
    @jakarta.validation.constraints.Size(max = 255)
    String name;

    @jakarta.validation.constraints.Size(max = 10000)
    String description;

    String audioUrl;
}
