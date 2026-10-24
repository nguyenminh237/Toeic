package com.example.toeic_api.modules.content.model;

import com.example.toeic_api.shared.model.OwnedCatalogEntity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Entity
@Table(
        name = "question_groups",
        indexes = @Index(name = "ix_group_part", columnList = "part_id,archived"))
public class QuestionGroup extends OwnedCatalogEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id")
    private ToeicPart part;

    private String title;
    private String audioUrl;
    private String imageUrl;
    private Integer audioStartMs;
    private Integer audioEndMs;

    @Column(columnDefinition = "LONGTEXT")
    private String transcript;

    @ElementCollection
    @CollectionTable(name = "group_documents", joinColumns = @JoinColumn(name = "group_id"))
    @OrderColumn(name = "position")
    private List<GroupDocument> documents = new ArrayList<>();
}
