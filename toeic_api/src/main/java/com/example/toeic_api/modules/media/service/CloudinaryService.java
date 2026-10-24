package com.example.toeic_api.modules.media.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.toeic_api.modules.media.dto.response.UploadResponse;
import com.example.toeic_api.shared.exception.AppException;
import com.example.toeic_api.shared.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class CloudinaryService {

    static final long IMAGE_MAX_BYTES = 10L * 1024 * 1024;
    static final long AUDIO_MAX_BYTES = 40L * 1024 * 1024;
    static final Set<String> IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    static final Set<String> AUDIO_TYPES =
            Set.of(
                    "audio/mpeg",
                    "audio/mp3",
                    "audio/wav",
                    "audio/x-wav",
                    "audio/wave",
                    "audio/mp4",
                    "audio/m4a",
                    "audio/x-m4a",
                    "audio/aac",
                    "audio/ogg",
                    "audio/webm");
    static final Set<String> IMAGE_EXTS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    static final Set<String> AUDIO_EXTS = Set.of("mp3", "wav", "m4a", "aac", "ogg", "webm");

    final Cloudinary cloudinary;

    @Value("${cloudinary.cloud-name}")
    String cloudName;

    @Value("${cloudinary.api-key}")
    String apiKey;

    @Value("${cloudinary.api-secret}")
    String apiSecret;

    @Value("${cloudinary.folder:toeic}")
    String folder;

    public UploadResponse upload(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_REQUIRED);
        }
        if (isBlank(cloudName)
                || isBlank(apiKey)
                || isBlank(apiSecret)
                || "YOUR_CLOUD_NAME".equals(cloudName)
                || "YOUR_API_KEY".equals(apiKey)
                || "YOUR_API_SECRET".equals(apiSecret)) {
            throw new AppException(ErrorCode.CLOUDINARY_NOT_CONFIGURED);
        }

        boolean audio = "audio".equalsIgnoreCase(type);
        boolean image = "image".equalsIgnoreCase(type);
        if (!audio && !image) {
            throw new AppException(ErrorCode.FILE_TYPE_INVALID);
        }

        validateFile(file, audio);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> params =
                    ObjectUtils.asMap(
                            "folder", folder + "/" + (audio ? "audio" : "images"),
                            "resource_type", audio ? "video" : "image",
                            "use_filename", true,
                            "unique_filename", true,
                            "overwrite", false);
            @SuppressWarnings("unchecked")
            Map<String, Object> result =
                    audio
                            ? cloudinary.uploader().uploadLarge(file.getInputStream(), params)
                            : cloudinary.uploader().upload(file.getBytes(), params);

            return UploadResponse.builder()
                    .url((String) result.get("secure_url"))
                    .publicId((String) result.get("public_id"))
                    .resourceType((String) result.get("resource_type"))
                    .format((String) result.get("format"))
                    .build();
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("Cloudinary upload failed", exception);
            String detail = exception.getMessage() == null ? "" : exception.getMessage();
            if (detail.toLowerCase(Locale.ROOT).contains("cloud_name")) {
                throw new AppException(ErrorCode.CLOUDINARY_INVALID_CLOUD_NAME);
            }
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }

    void validateFile(MultipartFile file, boolean audio) {
        long max = audio ? AUDIO_MAX_BYTES : IMAGE_MAX_BYTES;
        if (file.getSize() > max) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        String contentType =
                file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = extensionOf(file.getOriginalFilename());
        boolean typeOk =
                audio
                        ? AUDIO_TYPES.contains(contentType) || AUDIO_EXTS.contains(extension)
                        : IMAGE_TYPES.contains(contentType) || IMAGE_EXTS.contains(extension);
        if (!typeOk) {
            throw new AppException(ErrorCode.FILE_TYPE_INVALID);
        }
    }

    String extensionOf(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
