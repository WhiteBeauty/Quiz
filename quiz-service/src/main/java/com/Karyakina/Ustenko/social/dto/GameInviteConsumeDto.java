package com.Karyakina.Ustenko.social.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameInviteConsumeDto {

    @NotBlank
    private String token;
}
