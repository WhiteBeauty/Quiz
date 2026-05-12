package com.Karyakina.Ustenko.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GameRoomDto {
    private Long id;
    private String code;
    private Long quizId;
    private Long hostUserId;
    private String status;
    private List<ParticipantDto> participants;
    private LocalDateTime createdAt;
    private int currentQuestionIndex;
}
