package com.example.toeic_api.modules.practice.repository;

import com.example.toeic_api.modules.practice.model.PracticeAnswerEvent;

import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface PracticeAnswerEventRepository extends JpaRepository<PracticeAnswerEvent, Long> {}
