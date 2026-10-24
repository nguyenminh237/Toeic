package com.example.toeic_api.modules.practice.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "practice_answer_events",
        indexes = @Index(name = "ix_event_user_time", columnList = "user_id,answered_at"))
public class PracticeAnswerEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private long questionVersion;

    @Column(nullable = false)
    private long groupVersion;

    @Column(nullable = false)
    private String selectedAnswer;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private LocalDateTime answeredAt;
}
