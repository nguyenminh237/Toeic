package com.example.toeic_api.modules.media.repository;

import com.example.toeic_api.modules.media.model.PrivateMedia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrivateMediaRepository extends JpaRepository<PrivateMedia, String> {
    Optional<PrivateMedia> findByIdAndOwnerId(String id, String ownerId);
}
