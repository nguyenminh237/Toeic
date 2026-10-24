package com.example.toeic_api.modules.content.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "toeic_parts")
public class ToeicPart {
    @Id private Integer id;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String description;

    public boolean isListening() {
        return id <= 4;
    }

    public int optionCount() {
        return id == 2 ? 3 : 4;
    }
}
