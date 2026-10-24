package com.example.toeic_api.modules.tests.model;

import com.example.toeic_api.modules.users.model.User;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@Entity
@Table(
        name = "test_attempts",
        indexes = {
            @Index(name = "ix_attempt_user_time", columnList = "user_id,started_at"),
            @Index(name = "ix_attempt_expiry", columnList = "status,expires_at")
        })
public class TestAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id")
    private TestPart part;

    @Column(unique = true, length = 100)
    private String activeKey;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private int totalCount;

    private Integer timeSpent;
    private Integer correctCount;
    private Integer accuracyPercent;

    @Column(nullable = false)
    private int partNumber;

    @Column(nullable = false)
    private String suiteName;

    private String audioUrl;

    @OneToMany(
            mappedBy = "attempt",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("position ASC")
    private List<AttemptItem> items = new ArrayList<>();
}
