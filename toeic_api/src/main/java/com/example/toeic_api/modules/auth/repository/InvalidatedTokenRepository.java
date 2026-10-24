package com.example.toeic_api.modules.auth.repository;

import com.example.toeic_api.modules.auth.model.InvalidatedToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {}
