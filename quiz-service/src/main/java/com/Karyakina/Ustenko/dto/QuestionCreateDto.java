package com.Karyakina.Ustenko.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionCreateDto {

    @NotBlank(message = "Текст вопроса обязателен")
    private String text;

    @Min(value = 5, message = "Минимальное время ответа — 5 секунд")
    private int timeLimitSeconds = 30;

    @Min(value = 1, message = "Минимальное количество очков — 1")
    private int points = 10;

    private int orderIndex;

    @NotEmpty(message = "Необходимо указать варианты ответа")
    private List<AnswerOptionCreateDto> answerOptions;
}
