package com.Karyakina.Ustenko.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantDto {
    private Long id;
    private Long userId;
    private String username;
    private int totalScore;
    private boolean connected;
}
