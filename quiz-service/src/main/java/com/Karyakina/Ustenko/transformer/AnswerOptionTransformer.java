package com.Karyakina.Ustenko.transformer;

import org.springframework.stereotype.Component;
import com.Karyakina.Ustenko.dto.AnswerOptionCreateDto;
import com.Karyakina.Ustenko.dto.AnswerOptionDto;
import com.Karyakina.Ustenko.model.AnswerOption;
import com.Karyakina.Ustenko.model.Question;

@Component
public class AnswerOptionTransformer {

    public AnswerOptionDto toDto(AnswerOption option) {
        AnswerOptionDto dto = new AnswerOptionDto();
        dto.setId(option.getId());
        dto.setText(option.getText());
        dto.setCorrect(option.isCorrect());
        dto.setOrderIndex(option.getOrderIndex());
        return dto;
    }

    public AnswerOption fromCreateDto(AnswerOptionCreateDto dto, Question question) {
        AnswerOption option = new AnswerOption();
        option.setQuestion(question);
        option.setText(dto.getText());
        option.setCorrect(dto.isCorrect());
        option.setOrderIndex(dto.getOrderIndex());
        return option;
    }
}
