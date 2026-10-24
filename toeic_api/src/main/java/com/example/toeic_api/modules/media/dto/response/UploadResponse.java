package com.example.toeic_api.modules.media.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadResponse {
    String url;
    String publicId;
    String resourceType;
    String format;
}
