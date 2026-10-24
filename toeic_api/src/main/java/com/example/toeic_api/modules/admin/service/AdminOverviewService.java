package com.example.toeic_api.modules.admin.service;

import com.example.toeic_api.modules.tests.repository.TestAttemptRepository;
import com.example.toeic_api.modules.tests.repository.TestSuiteRepository;
import com.example.toeic_api.modules.tests.service.TestService;
import com.example.toeic_api.modules.users.mapper.UserMapper;
import com.example.toeic_api.modules.users.repository.UserRepository;
import com.example.toeic_api.modules.vocabulary.repository.VocabularyWordRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOverviewService {
    private final UserRepository users;
    private final VocabularyWordRepository words;
    private final TestSuiteRepository suites;
    private final TestAttemptRepository attempts;
    private final UserMapper mapper;
    private final TestService tests;
    private final JdbcTemplate jdbc;
    private final com.example.toeic_api.shared.security.ContentOwnership ownership;

    public Map<String, Object> getDashboard() {
        String owner = ownership.currentId();
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("userCount", users.count());
        v.put(
                "vocabularyWordCount",
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vocabulary_words WHERE owner_id=? AND archived=FALSE",
                        Long.class,
                        owner));
        v.put("testSuiteCount", suites.findByOwnerIdAndArchivedFalseOrderByIdAsc(owner).size());
        v.put("testResultCount", attempts.countByUserId(owner));
        for (var entry :
                Map.of(
                                "listeningQuestionCount",
                                new int[] {1, 4},
                                "readingQuestionCount",
                                new int[] {5, 7})
                        .entrySet())
            v.put(
                    entry.getKey(),
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM questions q JOIN question_groups g ON"
                                + " g.id=q.group_id WHERE q.archived=FALSE AND g.archived=FALSE AND"
                                + " g.part_id BETWEEN ? AND ? AND g.owner_id=?",
                            Long.class,
                            entry.getValue()[0],
                            entry.getValue()[1],
                            owner));
        v.put(
                "recentUsers",
                users.findTop5ByOrderByCreatedAtDesc().stream()
                        .map(mapper::toUserResponse)
                        .toList());
        v.put("recentTestResults", tests.getUserTestResults(null, 0, 8).items());
        return v;
    }
}
