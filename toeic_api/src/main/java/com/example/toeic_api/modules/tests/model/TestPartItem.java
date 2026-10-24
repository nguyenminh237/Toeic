package com.example.toeic_api.modules.tests.model;

import com.example.toeic_api.modules.content.model.Question;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "test_part_items",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_test_position",
                    columnNames = {"part_id", "position"}),
            @UniqueConstraint(
                    name = "uk_test_question",
                    columnNames = {"part_id", "question_id"})
        })
public class TestPartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id")
    private TestPart part;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(nullable = false)
    private int position;
}
