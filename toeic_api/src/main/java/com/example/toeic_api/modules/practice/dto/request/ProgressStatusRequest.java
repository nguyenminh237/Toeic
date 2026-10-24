package com.example.toeic_api.modules.practice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProgressStatusRequest {
    Long questionId;
    String status;
    Boolean bookmarked;
}
