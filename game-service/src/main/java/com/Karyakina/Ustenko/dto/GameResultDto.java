package com.Karyakina.Ustenko.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GameResultDto {
    private Long id;
    private Long gameRoomId;
    private Long userId;
    private String username;
    private int finalScore;
    private int rank;
    private int correctAnswers;
    private int totalQuestions;
    private LocalDateTime createdAt;
}
