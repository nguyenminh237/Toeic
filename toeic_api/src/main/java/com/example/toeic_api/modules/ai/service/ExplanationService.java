package com.example.toeic_api.modules.ai.service;

import com.example.toeic_api.modules.ai.dto.request.ExplanationRequest;
import com.example.toeic_api.modules.ai.dto.request.ExplanationRequest.Document;
import com.example.toeic_api.modules.content.service.ContentService;
import com.example.toeic_api.shared.exception.AppException;
import com.example.toeic_api.shared.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ExplanationService {
    private final GroqExplanationService groq;
    private final ContentService content;
    private final TransactionTemplate transactions;
    private final ObjectMapper mapper;

    public String generate(ExplanationRequest r) {
        Set<String> labels =
                r.partNumber() == 2 ? Set.of("A", "B", "C") : Set.of("A", "B", "C", "D");
        if (!labels.equals(r.options().keySet()) || !labels.contains(r.correctAnswer()))
            throw new AppException(
                    ErrorCode.INVALID_CONTENT, "Kiểm tra số lựa chọn và đáp án đúng.");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("partNumber", r.partNumber());
        data.put("questionText", r.questionText());
        data.put("promptText", r.promptText());
        data.put("correctAnswer", r.correctAnswer());
        data.put("options", r.options());
        // Copy shared context while the transaction is open; network calls happen after it closes.
        if (r.groupId() != null)
            transactions.executeWithoutResult(
                    tx -> {
                        var group = content.group(r.groupId());
                        if (group.getPart().getId() != r.partNumber())
                            throw new AppException(ErrorCode.INVALID_CONTENT);
                        data.put("transcript", group.getTranscript());
                        data.put(
                                "documents",
                                group.getDocuments().stream()
                                        .map(d -> new Document(d.getTitle(), d.getContent()))
                                        .toList());
                    });
        if (r.transcript() != null) data.put("transcript", r.transcript());
        if (r.documents() != null) data.put("documents", r.documents());
        var context = mapper.valueToTree(data);
        if ((r.partNumber() == 1 || r.partNumber() == 3 || r.partNumber() == 4)
                && context.path("transcript").asText("").isBlank())
            throw new AppException(
                    ErrorCode.INVALID_CONTENT,
                    "Hãy bổ sung transcript trong ngữ liệu chung trước khi tạo lời giải bài nghe.");
        if (r.partNumber() >= 6
                && !context.path("documents").findValues("content").stream()
                        .anyMatch(s -> s.isTextual() && !s.asText().isBlank()))
            throw new AppException(
                    ErrorCode.INVALID_CONTENT,
                    "Hãy bổ sung nội dung bài đọc trước khi tạo lời giải.");
        if (r.partNumber() == 2 || r.partNumber() == 5) {
            if (r.questionText() == null || r.questionText().isBlank())
                throw new AppException(
                        ErrorCode.INVALID_CONTENT,
                        "Hãy nhập nội dung câu hỏi trước khi tạo lời giải.");
        }
        return groq.generate(context);
    }
}
