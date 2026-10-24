package com.example.toeic_api.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class OwnedCatalogEntity extends CatalogEntity {
    // NULL is reserved for legacy content whose author cannot be established.
    @Column(name = "owner_id", updatable = false)
    private String ownerId;
}
