package com.example.toeic_api.modules.tests.repository;

import com.example.toeic_api.modules.tests.model.TestAttempt;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TestAttempt> findByActiveKey(String activeKey);

    org.springframework.data.domain.Page<TestAttempt> findByUserId(
            String userId, org.springframework.data.domain.Pageable pageable);

    long countByUserId(String userId);

    long countByUserIdAndStatus(String userId, String status);

    List<TestAttempt> findTop100ByStatusAndExpiresAtLessThanEqualOrderByExpiresAtAsc(
            String status, java.time.LocalDateTime time);

    List<TestAttempt> findTop8ByOrderByStartedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from TestAttempt a where a.id=:id")
    Optional<TestAttempt> lockById(@Param("id") Long id);
}
