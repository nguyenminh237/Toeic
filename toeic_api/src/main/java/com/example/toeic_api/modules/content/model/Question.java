package com.example.toeic_api.modules.content.model;

import com.example.toeic_api.shared.model.CatalogEntity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Entity
@Table(
        name = "questions",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_question_group_position",
                        columnNames = {"group_id", "position"}))
public class Question extends CatalogEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id")
    private QuestionGroup group;

    @Column(nullable = false)
    private int position;

    @Column(columnDefinition = "LONGTEXT")
    private String questionText;

    @Column(columnDefinition = "LONGTEXT")
    private String promptText;

    @Column(columnDefinition = "LONGTEXT")
    private String explanation;

    @Column(nullable = false)
    private boolean hideQuestion;

    @ElementCollection
    @CollectionTable(
            name = "question_options",
            joinColumns = @JoinColumn(name = "question_id"),
            uniqueConstraints =
                    @UniqueConstraint(
                            name = "uk_option_label",
                            columnNames = {"question_id", "label"}))
    @OrderColumn(name = "position")
    private List<QuestionOption> options = new ArrayList<>();

    public String correctAnswer() {
        return options.stream()
                .filter(QuestionOption::isCorrect)
                .map(QuestionOption::getLabel)
                .findFirst()
                .orElseThrow();
    }
}
