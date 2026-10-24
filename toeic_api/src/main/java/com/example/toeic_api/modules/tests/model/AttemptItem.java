package com.example.toeic_api.modules.tests.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "test_attempt_items",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_attempt_position",
                    columnNames = {"attempt_id", "position"}),
            @UniqueConstraint(
                    name = "uk_attempt_question",
                    columnNames = {"attempt_id", "question_id"})
        })
public class AttemptItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id")
    private TestAttempt attempt;

    @Column(nullable = false, updatable = false)
    private Long questionId;

    @Column(nullable = false, updatable = false)
    private int position;

    @Column(nullable = false, updatable = false, columnDefinition = "LONGTEXT")
    private String snapshot;

    private String selectedAnswer;
    private Boolean correct;
    private LocalDateTime answeredAt;
}
