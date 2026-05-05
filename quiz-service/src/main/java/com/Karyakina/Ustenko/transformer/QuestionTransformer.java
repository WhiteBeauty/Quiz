package com.Karyakina.Ustenko.transformer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.Karyakina.Ustenko.dto.AnswerOptionCreateDto;
import com.Karyakina.Ustenko.dto.QuestionCreateDto;
import com.Karyakina.Ustenko.dto.QuestionDto;
import com.Karyakina.Ustenko.model.AnswerOption;
import com.Karyakina.Ustenko.model.Question;
import com.Karyakina.Ustenko.model.Quiz;

@Component
@RequiredArgsConstructor
public class QuestionTransformer {

    private final AnswerOptionTransformer answerOptionTransformer;

    public QuestionDto toDto(Question question) {
        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setText(question.getText());
        dto.setTimeLimitSeconds(question.getTimeLimitSeconds());
        dto.setPoints(question.getPoints());
        dto.setOrderIndex(question.getOrderIndex());
        dto.setAnswerOptions(question.getAnswerOptions().stream()
                .map(answerOptionTransformer::toDto)
                .toList());
        return dto;
    }

    public Question fromCreateDto(QuestionCreateDto dto, Quiz quiz) {
        Question question = new Question();
        question.setQuiz(quiz);
        question.setText(dto.getText());
        question.setTimeLimitSeconds(dto.getTimeLimitSeconds());
        question.setPoints(dto.getPoints());
        question.setOrderIndex(dto.getOrderIndex());

        for (AnswerOptionCreateDto optionDto : dto.getAnswerOptions()) {
            AnswerOption option = answerOptionTransformer.fromCreateDto(optionDto, question);
            question.getAnswerOptions().add(option);
        }
        return question;
    }
}
