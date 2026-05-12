package com.Karyakina.Ustenko.social.service;

import org.springframework.security.core.Authentication;
import com.Karyakina.Ustenko.social.dto.GameInviteConsumeDto;
import com.Karyakina.Ustenko.social.dto.GameInviteCreateDto;
import com.Karyakina.Ustenko.social.dto.GameInviteResultDto;
import com.Karyakina.Ustenko.social.dto.GameInviteSendResponseDto;

public interface GameInviteService {

    GameInviteSendResponseDto createAndEmailInvite(GameInviteCreateDto dto, Authentication authentication);

    GameInviteResultDto consumeInvite(GameInviteConsumeDto dto, Authentication authentication);
}
