package com.example.toeic_api.modules.users.config;

import com.example.toeic_api.modules.users.model.User;
import com.example.toeic_api.modules.users.model.UserRole;
import com.example.toeic_api.modules.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Slf4j
public class ApplicationInitConfig implements ApplicationRunner {

    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;

    @Value("${app.admin.enabled:false}")
    boolean bootstrapEnabled;

    @Value("${app.admin.username:}")
    String adminUsername;

    @Value("${app.admin.password:}")
    String adminPassword;

    @Value("${app.admin.email:}")
    String adminEmail;

    @Value("${app.admin.full-name:System Admin}")
    String adminFullName;

    @Value("${app.admin.minimum-password-length:12}")
    int minimumPasswordLength;

    @Override
    public void run(ApplicationArguments args) {
        if (!bootstrapEnabled) return;
        if (minimumPasswordLength < 8
                || adminUsername.isBlank()
                || adminPassword.length() < minimumPasswordLength
                || adminEmail.isBlank())
            throw new IllegalStateException(
                    "Configure admin username, email and a password of at least "
                            + Math.max(8, minimumPasswordLength)
                            + " characters");
        if (userRepository.existsByUsername(adminUsername)) {
            log.info("Admin account already exists: {}", adminUsername);
            return;
        }

        User admin =
                User.builder()
                        .username(adminUsername)
                        .password(passwordEncoder.encode(adminPassword))
                        .email(adminEmail.trim().toLowerCase(java.util.Locale.ROOT))
                        .fullName(adminFullName)
                        .role(UserRole.ADMIN)
                        .enabled(true)
                        .build();
        userRepository.save(admin);
        log.info("Created default admin account: {}", adminUsername);
    }
}
