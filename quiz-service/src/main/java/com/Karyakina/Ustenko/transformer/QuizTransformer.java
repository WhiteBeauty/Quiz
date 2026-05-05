package com.Karyakina.Ustenko.transformer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.Karyakina.Ustenko.dto.QuizDto;
import com.Karyakina.Ustenko.model.Quiz;

@Component
@RequiredArgsConstructor
public class QuizTransformer {

    private final QuestionTransformer questionTransformer;

    public QuizDto toDto(Quiz quiz) {
        QuizDto dto = new QuizDto();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setCategory(quiz.getCategory());
        dto.setAuthorId(quiz.getAuthor().getId());
        dto.setAuthorUsername(quiz.getAuthor().getUsername());
        dto.setCreatedAt(quiz.getCreatedAt());
        dto.setPublished(quiz.isPublished());
        dto.setQuestionCount(quiz.getQuestions().size());
        dto.setQuestions(quiz.getQuestions().stream()
                .map(questionTransformer::toDto)
                .toList());
        return dto;
    }
}
