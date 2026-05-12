package com.Karyakina.Ustenko.social.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.Karyakina.Ustenko.social.dto.GameInviteConsumeDto;
import com.Karyakina.Ustenko.social.dto.GameInviteCreateDto;
import com.Karyakina.Ustenko.social.dto.GameInviteResultDto;
import com.Karyakina.Ustenko.social.dto.GameInviteSendResponseDto;
import com.Karyakina.Ustenko.social.service.GameInviteService;

@RestController
@RequestMapping("/api/game-invites")
@RequiredArgsConstructor
public class GameInviteController {

    private final GameInviteService gameInviteService;

    @PostMapping
    public ResponseEntity<GameInviteSendResponseDto> create(
            @Valid @RequestBody GameInviteCreateDto dto,
            Authentication authentication) {
        return ResponseEntity.ok(gameInviteService.createAndEmailInvite(dto, authentication));
    }

    @PostMapping("/consume")
    public ResponseEntity<GameInviteResultDto> consume(
            @Valid @RequestBody GameInviteConsumeDto dto,
            Authentication authentication) {
        return ResponseEntity.ok(gameInviteService.consumeInvite(dto, authentication));
    }
}
