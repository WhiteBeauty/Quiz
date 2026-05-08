package com.Karyakina.Ustenko.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoomDto {

    @NotNull(message = "ID квиза обязателен")
    private Long quizId;
}
