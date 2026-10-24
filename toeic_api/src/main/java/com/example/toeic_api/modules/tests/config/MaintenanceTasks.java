package com.example.toeic_api.modules.tests.config;

import com.example.toeic_api.modules.tests.repository.TestAttemptRepository;
import com.example.toeic_api.modules.tests.service.TestService;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;

import java.time.*;

@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.maintenance.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class MaintenanceTasks {
    private final TestAttemptRepository attempts;
    private final TestService tests;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${app.maintenance.delay-ms:60000}", initialDelay = 60000)
    public void finishExpired() {
        for (var a :
                attempts.findTop100ByStatusAndExpiresAtLessThanEqualOrderByExpiresAtAsc(
                        "IN_PROGRESS", LocalDateTime.now(clock))) tests.expire(a.getId());
    }
}
