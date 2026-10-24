package com.example.toeic_api.modules.media.service;

import com.example.toeic_api.modules.media.dto.response.UploadResponse;
import com.example.toeic_api.modules.media.model.PrivateMedia;
import com.example.toeic_api.modules.media.repository.PrivateMediaRepository;
import com.example.toeic_api.shared.exception.*;
import com.example.toeic_api.shared.security.ContentOwnership;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class PrivateMediaService {
    public static final String PREFIX = "/api/v1/workspace/media/";
    private final PrivateMediaRepository files;
    private final ContentOwnership ownership;
    private final Path root;

    public PrivateMediaService(
            PrivateMediaRepository files,
            ContentOwnership ownership,
            @Value("${app.media.storage-path:./.private-media}") String root) {
        this.files = files;
        this.ownership = ownership;
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    public PrivateMedia owned(String id) {
        return files.findByIdAndOwnerId(id, ownership.currentId())
                .orElseThrow(() -> new AppException(ErrorCode.CONTENT_NOT_FOUND));
    }

    public void validateReference(String url) {
        if (url != null && url.startsWith(PREFIX)) owned(url.substring(PREFIX.length()));
    }

    public FileSystemResource resource(PrivateMedia media) {
        Path file = root.resolve(media.getStorageKey()).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file))
            throw new AppException(ErrorCode.CONTENT_NOT_FOUND);
        return new FileSystemResource(file);
    }

    @Transactional
    public UploadResponse upload(MultipartFile file, String type) {
        String owner = ownership.currentId();
        if (file == null || file.isEmpty()) throw new AppException(ErrorCode.FILE_REQUIRED);
        if (!Set.of("image", "audio").contains(type))
            throw new AppException(ErrorCode.FILE_TYPE_INVALID);
        if (file.getSize() > (type.equals("image") ? 10L : 50L) * 1024 * 1024)
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        Path target = null;
        try {
            byte[] bytes = file.getBytes();
            String mime = detect(bytes, type);
            String id = UUID.randomUUID().toString();
            String key = id + ".bin";
            Files.createDirectories(root);
            target = root.resolve(key);
            Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
            final Path written = target;
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCompletion(int status) {
                            if (status != STATUS_COMMITTED)
                                try {
                                    Files.deleteIfExists(written);
                                } catch (IOException ignored) {
                                }
                        }
                    });
            var media = new PrivateMedia();
            media.setId(id);
            media.setOwnerId(owner);
            media.setStorageKey(key);
            String name =
                    Objects.toString(file.getOriginalFilename(), "file")
                            .replaceAll("[\\r\\n]", "_");
            media.setOriginalName(name.substring(0, Math.min(255, name.length())));
            media.setContentType(mime);
            media.setSizeBytes(bytes.length);
            media.setCreatedAt(LocalDateTime.now(Clock.systemUTC()));
            files.saveAndFlush(media);
            return UploadResponse.builder()
                    .url(PREFIX + id)
                    .publicId(id)
                    .resourceType(type)
                    .format(mime.substring(mime.indexOf('/') + 1))
                    .build();
        } catch (IOException e) {
            if (target != null)
                try {
                    Files.deleteIfExists(target);
                } catch (IOException ignored) {
                }
            throw new AppException(ErrorCode.UPLOAD_FAILED, "Không tải được file lên kho riêng. Thử lại sau.");
        }
    }

    private String detect(byte[] b, String type) {
        String head = new String(b, 0, Math.min(b.length, 16), StandardCharsets.ISO_8859_1);
        if (type.equals("image")) {
            if (head.startsWith("\u0089PNG\r\n\u001a\n")) return "image/png";
            if (b.length > 2 && (b[0] & 255) == 255 && (b[1] & 255) == 216) return "image/jpeg";
            if (head.startsWith("GIF87a") || head.startsWith("GIF89a")) return "image/gif";
            if (head.startsWith("RIFF")
                    && head.length() >= 12
                    && head.substring(8).startsWith("WEBP")) return "image/webp";
        } else {
            if (head.startsWith("ID3")) return "audio/mpeg";
            if (head.startsWith("RIFF")
                    && head.length() >= 12
                    && head.substring(8).startsWith("WAVE")) return "audio/wav";
            if (head.startsWith("OggS")) return "audio/ogg";
            if (head.length() >= 8 && head.substring(4).startsWith("ftyp")) return "audio/mp4";
            if (b.length > 2 && (b[0] & 255) == 255 && (b[1] & 224) == 224)
                return (b[1] & 246) == 240 ? "audio/aac" : "audio/mpeg";
        }
        throw new AppException(ErrorCode.FILE_TYPE_INVALID);
    }
}
