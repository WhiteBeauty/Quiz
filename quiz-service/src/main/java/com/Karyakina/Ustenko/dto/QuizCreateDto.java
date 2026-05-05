package com.Karyakina.Ustenko.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizCreateDto {

    @NotBlank(message = "Название квиза обязательно")
    private String title;

    private String description;
    private String category;
}
