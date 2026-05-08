package com.Karyakina.Ustenko.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitAnswerDto {

    @NotNull
    private Long questionId;

    private Long selectedOptionId;

    @NotNull
    private Long responseTimeMs;
}
