package com.Karyakina.Ustenko.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerOptionDto {
    private Long id;
    private String text;
    private boolean correct;
    private int orderIndex;
}
