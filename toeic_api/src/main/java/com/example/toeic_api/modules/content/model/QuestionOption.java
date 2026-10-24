package com.example.toeic_api.modules.content.model;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class QuestionOption {
    @Column(nullable = false, length = 1)
    private String label;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private boolean correct;
}
