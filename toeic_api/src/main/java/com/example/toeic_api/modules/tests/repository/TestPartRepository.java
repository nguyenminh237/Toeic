package com.example.toeic_api.modules.tests.repository;

import com.example.toeic_api.modules.tests.model.TestPart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface TestPartRepository extends JpaRepository<TestPart, Long> {
    List<TestPart> findBySuiteIdAndArchivedFalseOrderByToeicPartIdAsc(Long suiteId);
}
