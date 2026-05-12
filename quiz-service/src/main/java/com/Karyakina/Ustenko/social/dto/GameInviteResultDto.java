package com.Karyakina.Ustenko.social.dto;

import lombok.Getter;
import lombok.Setter;
import com.Karyakina.Ustenko.social.model.GameInviteType;

@Getter
@Setter
public class GameInviteResultDto {
    private String action;
    private String roomCode;
    private Long quizId;
    private GameInviteType inviteType;
}
