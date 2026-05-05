package com.Karyakina.Ustenko.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionDto {
    private Long id;
    private String text;
    private int timeLimitSeconds;
    private int points;
    private int orderIndex;
    private List<AnswerOptionDto> answerOptions;
}
