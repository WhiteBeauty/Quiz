package com.Karyakina.Ustenko.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.Karyakina.Ustenko.dto.GameRoomDto;
import com.Karyakina.Ustenko.dto.LeaderboardEntryDto;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameApiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${game-service.url}")
    private String gameServiceUrl;

    public GameRoomDto createRoom(Long quizId, String token) {
        return webClientBuilder.build()
                .post()
                .uri(gameServiceUrl + "/api/rooms")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(Map.of("quizId", quizId))
                .retrieve()
                .bodyToMono(GameRoomDto.class)
                .block();
    }

    public GameRoomDto joinRoom(String code, String token) {
        return webClientBuilder.build()
                .post()
                .uri(gameServiceUrl + "/api/rooms/" + code + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(GameRoomDto.class)
                .block();
    }

    public GameRoomDto getRoom(String code, String token) {
        return webClientBuilder.build()
                .get()
                .uri(gameServiceUrl + "/api/rooms/" + code)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(GameRoomDto.class)
                .block();
    }

    public GameRoomDto startGame(String code, String token) {
        return webClientBuilder.build()
                .post()
                .uri(gameServiceUrl + "/api/rooms/" + code + "/start")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(GameRoomDto.class)
                .block();
    }

    public GameRoomDto finishGame(String code, String token) {
        return webClientBuilder.build()
                .post()
                .uri(gameServiceUrl + "/api/rooms/" + code + "/finish")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(GameRoomDto.class)
                .block();
    }

    public List<LeaderboardEntryDto> getRoomLeaderboard(String code, String token) {
        return webClientBuilder.build()
                .get()
                .uri(gameServiceUrl + "/api/leaderboard/rooms/" + code)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GameRoomDto>>() {})
                .block();
    }
}
