package com.example.toeic_api.modules.auth.service;

import com.example.toeic_api.modules.auth.model.PasswordResetToken;
import com.example.toeic_api.modules.auth.repository.PasswordResetRepository;
import com.example.toeic_api.modules.users.repository.UserRepository;
import com.example.toeic_api.shared.exception.AppException;
import com.example.toeic_api.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.*;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final PasswordResetRepository tokens;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final ObjectProvider<JavaMailSender> mail;
    private final Clock clock;

    @Value("${app.password-reset.enabled:false}")
    private boolean enabled;

    @Value("${app.password-reset.frontend-url:http://localhost:5173}")
    private String frontend;

    @Value("${app.password-reset.from:no-reply@toeic.local}")
    private String from;

    @Transactional
    public void request(String email) {
        if (!enabled || mail.getIfAvailable() == null)
            throw new AppException(ErrorCode.RESET_UNAVAILABLE);
        var found = users.findByEmail(email.trim().toLowerCase(Locale.ROOT));
        if (found.isEmpty() || !Boolean.TRUE.equals(found.get().getEnabled())) return;
        byte[] entropy = new byte[32];
        new SecureRandom().nextBytes(entropy);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
        var token = new PasswordResetToken();
        token.setTokenHash(hash(raw));
        token.setUser(found.get());
        token.setAuthVersion(found.get().getAuthVersion());
        token.setExpiresAt(LocalDateTime.now(clock).plusMinutes(20));
        tokens.save(token);
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(found.get().getEmail());
        message.setSubject("Đặt lại mật khẩu TOEICUP");
        message.setText(
                "Mở liên kết trong 20 phút để đặt lại mật khẩu: "
                        + frontend.replaceAll("/$", "")
                        + "/reset-password/"
                        + raw);
        try {
            mail.getObject().send(message);
        } catch (Exception e) {
            throw new AppException(ErrorCode.RESET_UNAVAILABLE);
        }
    }

    @Transactional
    public void reset(String raw, String password) {
        if (raw == null || raw.length() > 200)
            throw new AppException(ErrorCode.INVALID_RESET_TOKEN);
        var token =
                tokens.lockByHash(hash(raw))
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_RESET_TOKEN));
        var user = users.lockById(token.getUser().getId()).orElseThrow();
        if (token.getUsedAt() != null
                || !LocalDateTime.now(clock).isBefore(token.getExpiresAt())
                || token.getAuthVersion() != user.getAuthVersion()
                || !Boolean.TRUE.equals(user.getEnabled()))
            throw new AppException(ErrorCode.INVALID_RESET_TOKEN);
        user.setPassword(encoder.encode(password));
        user.setAuthVersion(user.getAuthVersion() + 1);
        token.setUsedAt(LocalDateTime.now(clock));
    }

    public static String hash(String raw) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
