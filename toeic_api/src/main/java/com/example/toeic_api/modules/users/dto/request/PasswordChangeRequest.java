package com.example.toeic_api.modules.users.dto.request;

public record PasswordChangeRequest(
        @jakarta.validation.constraints.NotBlank String currentPassword,
        @jakarta.validation.constraints.Size(min = 6, max = 100)
                @jakarta.validation.constraints.NotBlank
                String newPassword) {}
