package com.Karyakina.Ustenko.service;

import com.Karyakina.Ustenko.dto.GameResultDto;
import com.Karyakina.Ustenko.dto.LeaderboardEntryDto;

import java.util.List;

public interface LeaderboardService {
    List<LeaderboardEntryDto> getRoomLeaderboard(String roomCode);
    List<GameResultDto> getPlayerHistory(Long userId);
    List<LeaderboardEntryDto> getGlobalTop10();

    void saveGameResults(String roomCode, int totalQuestions);
}
