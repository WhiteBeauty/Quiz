package com.Karyakina.Ustenko.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.Karyakina.Ustenko.config.JwtService;
import com.Karyakina.Ustenko.dto.CreateRoomDto;
import com.Karyakina.Ustenko.dto.GameRoomDto;
import com.Karyakina.Ustenko.dto.LeaderboardEntryDto;
import com.Karyakina.Ustenko.service.GameRoomService;
import com.Karyakina.Ustenko.service.LeaderboardService;
import com.Karyakina.Ustenko.websocket.GameWebSocketController;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class GameRoomController {

    private final GameRoomService gameRoomService;
    private final GameWebSocketController gameWebSocketController;
    private final JwtService jwtService;
    private final LeaderboardService leaderboardService;

    @PostMapping
    public ResponseEntity<GameRoomDto> createRoom(
            @Valid @RequestBody CreateRoomDto dto,
            Authentication authentication,
            HttpServletRequest request) {

        Long userId = extractUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        GameRoomDto room = gameRoomService.createRoom(dto, userId, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    @PostMapping("/{code}/join")
    public ResponseEntity<GameRoomDto> joinRoom(
            @PathVariable String code,
            Authentication authentication,
            HttpServletRequest request) {

        Long userId = extractUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        GameRoomDto room = gameRoomService.joinRoom(code, userId, username);
        gameWebSocketController.broadcastPlayerJoined(code, username);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/{code}")
    public ResponseEntity<GameRoomDto> findByCode(@PathVariable String code) {
        GameRoomDto room = gameRoomService.findByCode(code);
        return ResponseEntity.ok(room);
    }

    @PostMapping("/{code}/start")
    public ResponseEntity<GameRoomDto> startGame(
            @PathVariable String code,
            Authentication authentication,
            HttpServletRequest request) {

        Long userId = extractUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = extractTokenFromRequest(request);
        GameRoomDto room = gameRoomService.startGame(code, userId, token);
        gameWebSocketController.broadcastGameStarted(code, room);
        return ResponseEntity.ok(room);
    }

    @PostMapping("/{code}/next")
    public ResponseEntity<GameRoomDto> nextQuestion(
            @PathVariable String code,
            Authentication authentication,
            HttpServletRequest request) {

        Long userId = extractUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = extractTokenFromRequest(request);
        GameRoomDto room = gameRoomService.nextQuestion(code, userId);
        gameWebSocketController.broadcastNextQuestion(code, room);
        return ResponseEntity.ok(room);
    }

    @PostMapping("/{code}/finish")
    public ResponseEntity<GameRoomDto> finishGame(
            @PathVariable String code,
            HttpServletRequest request) {

        Long userId = extractUserIdFromRequest(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        GameRoomDto room = gameRoomService.finishGame(code, userId);
        leaderboardService.saveGameResults(code, 0);
        List<LeaderboardEntryDto> leaderboard = leaderboardService.getRoomLeaderboard(code);
        gameWebSocketController.broadcastGameFinished(code, leaderboard);

        return ResponseEntity.ok(room);
    }

    @GetMapping("/my")
    public ResponseEntity<List<GameRoomDto>> findMyRooms(
            Authentication authentication,
            HttpServletRequest request) {

        Long userId = extractUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<GameRoomDto> rooms = gameRoomService.findRoomsByHost(userId);
        return ResponseEntity.ok(rooms);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        try {
            return jwtService.extractUserId(token);
        } catch (Exception e) {
            return null;
        }
    }
}