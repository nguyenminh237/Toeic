package com.example.toeic_api.modules.users.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank(message = "USERNAME_REQUIRED")
    @Size(min = 3, max = 50, message = "INVALID_USERNAME")
    String username;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, max = 100, message = "INVALID_PASSWORD")
    String password;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    String email;

    String fullName;
}
