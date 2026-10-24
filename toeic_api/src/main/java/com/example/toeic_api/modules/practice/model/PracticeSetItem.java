package com.example.toeic_api.modules.practice.model;

import com.example.toeic_api.modules.content.model.Question;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "practice_set_items",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_practice_position",
                    columnNames = {"set_id", "position"}),
            @UniqueConstraint(
                    name = "uk_practice_question",
                    columnNames = {"set_id", "question_id"})
        })
public class PracticeSetItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "set_id")
    private PracticeSet set;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(nullable = false)
    private int position;
}
