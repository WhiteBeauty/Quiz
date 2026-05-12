package com.Karyakina.Ustenko.social.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import com.Karyakina.Ustenko.social.model.GameInviteType;

@Getter
@Setter
public class GameInviteCreateDto {

    @NotNull
    private Long friendUserId;

    @NotNull
    private GameInviteType inviteType;

    /** Для JOIN_EXISTING */
    private String roomCode;

    /** Для CREATE_NEW_GAME */
    private Long quizId;
}
