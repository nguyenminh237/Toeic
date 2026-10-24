package com.example.toeic_api.modules.vocabulary.service;

import com.example.toeic_api.modules.ai.service.GroqVocabularyService;
import com.example.toeic_api.modules.vocabulary.dto.request.*;
import com.example.toeic_api.modules.vocabulary.dto.response.VocabularyWordResponse;
import com.example.toeic_api.shared.exception.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class VocabularyImportService {
    private final VocabularyService vocabulary;
    private final com.example.toeic_api.modules.vocabulary.repository.VocabularySetRepository sets;
    private final com.example.toeic_api.modules.vocabulary.repository.VocabularyWordRepository
            words;

    @Transactional
    public List<VocabularyWordResponse> confirm(VocabularyImportRequests.Confirm request) {
        if (!request.reviewed())
            throw new AppException(
                    ErrorCode.INVALID_CONTENT, "Hãy duyệt nội dung AI trước khi lưu.");
        vocabulary.ownedSet(request.setId());
        sets.lockForImport(request.setId())
                .filter(s -> !s.isArchived() && !s.getCategory().isArchived())
                .orElseThrow(() -> new AppException(ErrorCode.VOCABULARY_SET_NOT_FOUND));
        var existing = new HashSet<>(words.activeTermsBySet(request.setId()));
        var seen = new HashSet<String>();
        for (var word : request.words()) {
            String term = GroqVocabularyService.englishTerm(word.getWord());
            if (!Objects.equals(word.getSetId(), request.setId()) || !seen.add(term))
                throw new AppException(
                        ErrorCode.INVALID_CONTENT, "Bộ nhận hoặc danh sách từ không hợp lệ.");
            if (existing.contains(term))
                throw new AppException(
                        ErrorCode.DATA_CONFLICT,
                        "Từ đã có trong bộ: " + term + ". Bỏ chọn từ này hoặc sửa bản hiện có.");
        }
        return request.words().stream().map(vocabulary::createWord).toList();
    }
}
