package com.example.toeic_api.modules.auth.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    @jakarta.validation.constraints.NotBlank String username;
    @jakarta.validation.constraints.NotBlank String password;
}
