package com.Karyakina.Ustenko.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.Karyakina.Ustenko.dto.LeaderboardEntryDto;
import com.Karyakina.Ustenko.dto.WsMessageDto;
import com.Karyakina.Ustenko.service.LeaderboardService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GameWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final LeaderboardService leaderboardService;

    @MessageMapping("/game/{roomCode}/ping")
    @SendTo("/topic/game/{roomCode}")
    public WsMessageDto handlePing(@DestinationVariable String roomCode, String username) {
        return new WsMessageDto("PONG", "Соединение активно: " + username);
    }

    public void broadcastPlayerJoined(String roomCode, String username) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + roomCode,
                new WsMessageDto("PLAYER_JOINED", username)
        );
    }

    public void broadcastGameStarted(String roomCode, Object firstQuestion) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + roomCode,
                new WsMessageDto("GAME_STARTED", firstQuestion)
        );
    }

    public void broadcastNextQuestion(String roomCode, Object question) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + roomCode,
                new WsMessageDto("NEXT_QUESTION", question)
        );
    }

    public void broadcastLeaderboard(String roomCode) {
        List<LeaderboardEntryDto> leaderboard = leaderboardService.getRoomLeaderboard(roomCode);
        messagingTemplate.convertAndSend(
                "/topic/game/" + roomCode,
                new WsMessageDto("LEADERBOARD_UPDATE", leaderboard)
        );
    }

    public void broadcastGameFinished(String roomCode, List<LeaderboardEntryDto> leaderboard) {
        messagingTemplate.convertAndSend(
                "/topic/game/" + roomCode,
                new WsMessageDto("GAME_FINISHED", leaderboard)
        );
    }

    public void broadcastAnswerResult(String roomCode, Long userId, boolean correct, int points) {
        messagingTemplate.convertAndSend(
                "/queue/game/" + roomCode + "/user/" + userId,
                new WsMessageDto("ANSWER_RESULT", new Object[]{correct, points})
        );
    }
}
