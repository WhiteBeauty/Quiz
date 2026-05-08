package com.Karyakina.Ustenko.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.Karyakina.Ustenko.config.JwtService;
import com.Karyakina.Ustenko.dto.GameResultDto;
import com.Karyakina.Ustenko.dto.LeaderboardEntryDto;
import com.Karyakina.Ustenko.service.LeaderboardService;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final JwtService jwtService;

    @GetMapping("/rooms/{roomCode}")
    public ResponseEntity<List<LeaderboardEntryDto>> getRoomLeaderboard(@PathVariable String roomCode) {
        return ResponseEntity.ok(leaderboardService.getRoomLeaderboard(roomCode));
    }

    @GetMapping("/global")
    public ResponseEntity<List<LeaderboardEntryDto>> getGlobalTop10() {
        return ResponseEntity.ok(leaderboardService.getGlobalTop10());
    }

    @GetMapping("/history")
    public ResponseEntity<List<GameResultDto>> getMyHistory(Authentication authentication) {
        String token = (String) authentication.getCredentials();
        Long userId = jwtService.extractUserId(token);
        return ResponseEntity.ok(leaderboardService.getPlayerHistory(userId));
    }
}
