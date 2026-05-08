package com.Karyakina.Ustenko.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LeaderboardEntryDto {
    private int rank;
    private String username;
    private int score;
    private int correctAnswers;
}
