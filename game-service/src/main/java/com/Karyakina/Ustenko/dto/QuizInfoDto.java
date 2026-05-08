package com.Karyakina.Ustenko.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizInfoDto {
    private Long id;
    private String title;
    private String description;
    private List<QuizQuestionDto> questions;
}
