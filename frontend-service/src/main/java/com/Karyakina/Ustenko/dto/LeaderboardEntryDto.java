package com.Karyakina.Ustenko.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaderboardEntryDto {
    private int rank;
    private String username;
    private int score;
    private int correctAnswers;
}
