package com.example.toeic_api.modules.users.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
    String email;
    String fullName;
    String role;
    Boolean enabled;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
