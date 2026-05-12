package com.Karyakina.Ustenko.dto.social;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameInviteResultDto {
    private String action;
    private String roomCode;
    private Long quizId;
    private String inviteType;
}
