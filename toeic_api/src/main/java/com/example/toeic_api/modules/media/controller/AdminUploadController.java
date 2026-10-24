package com.example.toeic_api.modules.media.controller;

import com.example.toeic_api.modules.media.dto.response.UploadResponse;
import com.example.toeic_api.modules.media.service.PrivateMediaService;
import com.example.toeic_api.shared.api.ApiResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/api/v1/admin/uploads", "/api/v1/workspace/uploads"})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated()")
public class AdminUploadController {

    PrivateMediaService privateMedia;

    @PostMapping
    ApiResponse<UploadResponse> upload(
            @RequestParam("file") MultipartFile file, @RequestParam("type") String type) {
        return ApiResponse.<UploadResponse>builder()
                .result(privateMedia.upload(file, type))
                .build();
    }
}
