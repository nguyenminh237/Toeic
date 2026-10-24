package com.example.toeic_api.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    @NotBlank(message = "INVALID_PASSWORD")
    @Size(min = 6, message = "INVALID_PASSWORD")
    String newPassword;

    @NotBlank String token;
}
