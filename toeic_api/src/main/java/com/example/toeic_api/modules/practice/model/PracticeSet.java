package com.example.toeic_api.modules.practice.model;

import com.example.toeic_api.modules.content.model.ToeicPart;
import com.example.toeic_api.shared.model.OwnedCatalogEntity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "practice_sets",
        indexes = @Index(name = "ix_practice_part", columnList = "part_id,archived,id"))
public class PracticeSet extends OwnedCatalogEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id")
    private ToeicPart part;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    private String audioUrl;
}
