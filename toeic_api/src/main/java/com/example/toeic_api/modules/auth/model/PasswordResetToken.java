package com.example.toeic_api.modules.auth.model;

import com.example.toeic_api.modules.users.model.User;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "password_reset_tokens",
        indexes = @Index(name = "ix_reset_expiry", columnList = "expires_at"))
public class PasswordResetToken {
    @Id
    @Column(length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    @Column(nullable = false)
    private long authVersion;
}
