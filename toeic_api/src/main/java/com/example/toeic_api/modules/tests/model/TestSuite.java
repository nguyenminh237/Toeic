package com.example.toeic_api.modules.tests.model;

import com.example.toeic_api.shared.model.OwnedCatalogEntity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "test_suites")
public class TestSuite extends OwnedCatalogEntity {
    @Column(nullable = false, unique = true)
    private String suiteName;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    private String source;
}
