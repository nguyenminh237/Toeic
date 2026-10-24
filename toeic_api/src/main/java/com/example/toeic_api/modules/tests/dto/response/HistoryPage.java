package com.example.toeic_api.modules.tests.dto.response;

import java.util.List;

public record HistoryPage(
        List<TestViews.Attempt> items, int page, int size, long totalElements, int totalPages) {}
