package com.example.toeic_api.modules.tests.model;

import com.example.toeic_api.modules.content.model.ToeicPart;
import com.example.toeic_api.shared.model.CatalogEntity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "test_parts",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_suite_part",
                        columnNames = {"suite_id", "toeic_part_id"}))
public class TestPart extends CatalogEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "suite_id")
    private TestSuite suite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "toeic_part_id")
    private ToeicPart toeicPart;

    @Column(nullable = false)
    private Integer timeLimit;

    private String audioUrl;
}
