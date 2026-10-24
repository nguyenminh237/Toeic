package com.example.toeic_api.modules.practice.model;

import com.example.toeic_api.modules.content.model.Question;
import com.example.toeic_api.modules.users.model.User;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "user_question_progress",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_progress_user_question",
                        columnNames = {"user_id", "question_id"}),
        indexes = @Index(name = "ix_progress_user_time", columnList = "user_id,last_answered_at"))
public class QuestionProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    private String selectedAnswer;

    @Column(nullable = false)
    private long questionVersion;

    @Column(nullable = false)
    private long groupVersion;

    private Boolean correct;

    @Column(nullable = false)
    private boolean bookmarked;

    @Column(nullable = false)
    private long attemptCount;

    private LocalDateTime lastAnsweredAt;
}
