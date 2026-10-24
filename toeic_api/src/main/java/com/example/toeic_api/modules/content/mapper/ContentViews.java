package com.example.toeic_api.modules.content.mapper;

import com.example.toeic_api.modules.content.dto.response.QuestionView;
import com.example.toeic_api.modules.content.model.GroupDocument;
import com.example.toeic_api.modules.content.model.Question;
import com.example.toeic_api.modules.content.model.QuestionGroup;
import com.example.toeic_api.modules.content.model.QuestionOption;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ContentViews {
    public QuestionView question(Question q, int number, boolean solution) {
        QuestionView v = new QuestionView();
        QuestionGroup g = q.getGroup();
        v.setId(q.getId());
        if (!g.getDocuments().isEmpty()) v.setPassageId(g.getId());
        v.setVersion(q.getVersion());
        v.setPartNumber(g.getPart().getId());
        v.setQuestionNumber(number);
        v.setQuestionText(q.getQuestionText());
        v.setPromptText(q.getPromptText());
        v.setHideQuestion(q.isHideQuestion());
        v.setAudioUrl(g.getAudioUrl());
        v.setImageUrl(g.getImageUrl());
        v.setAudioStartMs(g.getAudioStartMs());
        v.setAudioEndMs(g.getAudioEndMs());
        v.setGroup(
                new QuestionView.Group(
                        g.getId(),
                        g.getTitle(),
                        g.getAudioUrl(),
                        g.getImageUrl(),
                        g.getAudioStartMs(),
                        g.getAudioEndMs(),
                        solution ? g.getTranscript() : null,
                        g.getDocuments().stream()
                                .map(
                                        d ->
                                                new GroupDocument(
                                                        d.getTitle(),
                                                        d.getContent(),
                                                        d.getImageUrl()))
                                .toList()));
        v.setOptions(
                q.getOptions().stream()
                        .map(o -> new QuestionView.Option(o.getLabel(), o.getContent()))
                        .toList());
        for (QuestionOption o : q.getOptions()) {
            switch (o.getLabel()) {
                case "A" -> v.setOptionA(o.getContent());
                case "B" -> v.setOptionB(o.getContent());
                case "C" -> v.setOptionC(o.getContent());
                case "D" -> v.setOptionD(o.getContent());
                default -> {}
            }
        }
        if (solution) {
            v.setCorrectAnswer(q.correctAnswer());
            v.setExplanation(q.getExplanation());
        }
        return v;
    }

    public QuestionView hideSolution(QuestionView v) {
        v.setCorrectAnswer(null);
        v.setExplanation(null);
        var g = v.getGroup();
        if (g != null)
            v.setGroup(
                    new QuestionView.Group(
                            g.id(),
                            g.title(),
                            g.audioUrl(),
                            g.imageUrl(),
                            g.audioStartMs(),
                            g.audioEndMs(),
                            null,
                            g.documents()));
        return v;
    }
}
