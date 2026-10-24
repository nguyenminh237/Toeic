package com.example.toeic_api.modules.media.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    String cloudName;

    @Value("${cloudinary.api-key}")
    String apiKey;

    @Value("${cloudinary.api-secret}")
    String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        String normalizedCloudName = cloudName == null ? "" : cloudName.trim().toLowerCase();
        return new Cloudinary(
                ObjectUtils.asMap(
                        "cloud_name",
                        normalizedCloudName,
                        "api_key",
                        apiKey == null ? "" : apiKey.trim(),
                        "api_secret",
                        apiSecret == null ? "" : apiSecret.trim(),
                        "secure",
                        true));
    }
}
