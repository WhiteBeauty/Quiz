package com.Karyakina.Ustenko.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.Karyakina.Ustenko.dto.GameRoomDto;
import com.Karyakina.Ustenko.dto.LeaderboardEntryDto;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${game-service.url}")
    private String gameServiceUrl;

    public GameRoomDto createRoom(Long quizId, String token) {
        return webClientBuilder.build()
                .post()
                .uri(gameServiceUrl + "/api/rooms")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of("quizId", quizId))
                .retrieve()
                .bodyToMono(GameRoomDto.class)
                .block();
    }

    public GameRoomDto joinRoom(String roomCode, String token) {
        return webClientBuilder.build()
                .post()
                .uri(gameServiceUrl + "/api/rooms/" + roomCode + "/join")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(GameRoomDto.class)
                .block();
    }

    public GameRoomDto getRoom(String roomCode, String token) {
        return webClientBuilder.build()
                .get()
                .uri(gameServiceUrl + "/api/rooms/" + roomCode)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(GameRoomDto.class)
                .block();
    }

    public GameRoomDto startGame(String roomCode, String token) {
        return webClientBuilder.build()
                .post()
                .uri(gameServiceUrl + "/api/rooms/" + roomCode + "/start")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(GameRoomDto.class)
                .block();
    }
// попытки сделать переключение между вопросами (безуспешно)
    public GameRoomDto nextQuestion(String roomCode, String token) {
        return webClientBuilder.build()
                .post()
                .uri(gameServiceUrl + "/api/rooms/" + roomCode + "/next")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(GameRoomDto.class)
                .block();
    }

    public GameRoomDto finishGame(String roomCode, String token) {
        return webClientBuilder.build()
                .post()
                .uri(gameServiceUrl + "/api/rooms/" + roomCode + "/finish")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(GameRoomDto.class)
                .block();
    }

    public void submitAnswer(String roomCode, Long questionId, Long selectedOptionId,
                             Long responseTimeMs, String token) {
        webClientBuilder.build()
                .post()
                .uri(gameServiceUrl + "/api/rooms/" + roomCode + "/answers")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of(
                        "questionId", questionId,
                        "selectedOptionId", selectedOptionId,
                        "responseTimeMs", responseTimeMs
                ))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public List<LeaderboardEntryDto> getRoomLeaderboard(String roomCode, String token) {
        return webClientBuilder.build()
                .get()
                .uri(gameServiceUrl + "/api/leaderboard/rooms/" + roomCode)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<LeaderboardEntryDto>>() {})
                .block();
    }

    public List<LeaderboardEntryDto> getGlobalTop10() {
        return webClientBuilder.build()
                .get()
                .uri(gameServiceUrl + "/api/leaderboard/global")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<LeaderboardEntryDto>>() {})
                .block();
    }

    public List<GameRoomDto> getMyRooms(String token) {
        return webClientBuilder.build()
                .get()
                .uri(gameServiceUrl + "/api/rooms/my")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GameRoomDto>>() {})
                .block();
    }
}
