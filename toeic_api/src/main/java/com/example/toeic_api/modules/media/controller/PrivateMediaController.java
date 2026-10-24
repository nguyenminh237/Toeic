package com.example.toeic_api.modules.media.controller;

import com.example.toeic_api.modules.media.service.PrivateMediaService;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workspace/media")
public class PrivateMediaController {
    private final PrivateMediaService media;

    @GetMapping("/{id}")
    public ResponseEntity<Resource> get(@PathVariable String id) {
        var file = media.owned(id);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("media").build().toString())
                .body(media.resource(file));
    }
}
