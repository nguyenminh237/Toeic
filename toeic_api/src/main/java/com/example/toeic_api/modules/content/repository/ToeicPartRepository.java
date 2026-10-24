package com.example.toeic_api.modules.content.repository;

import com.example.toeic_api.modules.content.model.ToeicPart;

import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface ToeicPartRepository extends JpaRepository<ToeicPart, Integer> {
    List<ToeicPart> findAllByOrderByIdAsc();
}
