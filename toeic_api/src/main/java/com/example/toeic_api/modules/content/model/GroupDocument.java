package com.example.toeic_api.modules.content.model;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class GroupDocument {
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private String imageUrl;
}
