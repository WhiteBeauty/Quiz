package com.Karyakina.Ustenko.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAnswerOptionDto {
    private Long id;
    private String text;
    private int orderIndex;
    private boolean correct;
}
