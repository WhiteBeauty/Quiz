package com.Karyakina.Ustenko.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerOptionCreateDto {

    @NotBlank(message = "Текст варианта ответа обязателен")
    private String text;

    private boolean correct;
    private int orderIndex;
}
