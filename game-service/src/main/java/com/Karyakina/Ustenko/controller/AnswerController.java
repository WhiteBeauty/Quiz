package com.Karyakina.Ustenko.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.Karyakina.Ustenko.config.JwtService;
import com.Karyakina.Ustenko.dao.GameParticipantDao;
import com.Karyakina.Ustenko.dao.PlayerAnswerDao;
import com.Karyakina.Ustenko.dto.SubmitAnswerDto;
import com.Karyakina.Ustenko.model.PlayerAnswer;
import com.Karyakina.Ustenko.service.AnswerService;
import com.Karyakina.Ustenko.websocket.GameWebSocketController;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;
    private final GameWebSocketController gameWebSocketController;
    private final JwtService jwtService;
    private final PlayerAnswerDao playerAnswerDao;
    private final GameParticipantDao gameParticipantDao;

    @PostMapping("/{roomCode}/answers")
    public ResponseEntity<Void> submitAnswer(
            @PathVariable String roomCode,
            @Valid @RequestBody SubmitAnswerDto dto,
            Authentication authentication) {
        String token = (String) authentication.getCredentials();
        Long userId = extractUserIdFromToken(token);

        PlayerAnswer answer = answerService.submitAnswer(roomCode, userId, dto, token);

        gameWebSocketController.broadcastAnswerResult(roomCode, userId, answer.isCorrect(), answer.getPointsEarned());
        long participantsCount = gameParticipantDao.countByGameRoomId(answer.getGameRoomId());
        long answersCount = playerAnswerDao.countByGameRoomIdAndQuestionId(
                answer.getGameRoomId(),
                answer.getQuestionId()
        );
        if (participantsCount > 0 && answersCount >= participantsCount) {
            gameWebSocketController.broadcastLeaderboard(roomCode);
        }

        return ResponseEntity.ok().build();
    }

    private Long extractUserIdFromToken(String token) {
        try {
            return jwtService.extractUserId(token);
        } catch (Exception e) {
            return 0L;
        }
    }
}
