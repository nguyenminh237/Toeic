package com.example.toeic_api.modules.auth.repository;

import com.example.toeic_api.modules.auth.model.PasswordResetToken;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordResetToken, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PasswordResetToken t where t.tokenHash=:hash")
    Optional<PasswordResetToken> lockByHash(@Param("hash") String hash);
}
