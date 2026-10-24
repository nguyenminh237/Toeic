package com.example.toeic_api.modules.vocabulary.model;

import com.example.toeic_api.modules.users.model.User;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "user_vocabulary_collection")
public class UserVocabularyCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false)
    String setName; // 'Từ vựng của tôi' (default) or custom name

    @Column(nullable = false)
    Boolean isDefault; // true if 'Từ vựng của tôi'

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(java.time.Clock.systemUTC());
    }

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<UserVocabularyItem> items;
}
