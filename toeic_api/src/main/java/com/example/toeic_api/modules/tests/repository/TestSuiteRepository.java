package com.example.toeic_api.modules.tests.repository;

import com.example.toeic_api.modules.tests.model.TestSuite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface TestSuiteRepository extends JpaRepository<TestSuite, Long> {
    java.util.List<TestSuite> findByOwnerIdAndArchivedFalseOrderByIdAsc(String ownerId);

    List<TestSuite> findByArchivedFalseOrderByIdAsc();
}
