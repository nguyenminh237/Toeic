package com.example.toeic_api.modules.content.dto.response;

import java.util.List;

public record ContentBundleResponse(Long id, long version, List<QuestionView> questions) {}
