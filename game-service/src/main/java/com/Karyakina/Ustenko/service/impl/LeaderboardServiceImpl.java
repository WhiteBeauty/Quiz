package com.Karyakina.Ustenko.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Karyakina.Ustenko.dao.GameParticipantDao;
import com.Karyakina.Ustenko.dao.GameResultDao;
import com.Karyakina.Ustenko.dao.GameRoomDao;
import com.Karyakina.Ustenko.dao.PlayerAnswerDao;
import com.Karyakina.Ustenko.dto.GameResultDto;
import com.Karyakina.Ustenko.dto.LeaderboardEntryDto;
import com.Karyakina.Ustenko.model.GameParticipant;
import com.Karyakina.Ustenko.model.GameResult;
import com.Karyakina.Ustenko.model.GameRoom;
import com.Karyakina.Ustenko.service.LeaderboardService;
import com.Karyakina.Ustenko.transformer.GameResultTransformer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaderboardServiceImpl implements LeaderboardService {

    private final GameRoomDao gameRoomDao;
    private final GameParticipantDao gameParticipantDao;
    private final PlayerAnswerDao playerAnswerDao;
    private final GameResultDao gameResultDao;
    private final GameResultTransformer gameResultTransformer;

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> getRoomLeaderboard(String roomCode) {
        GameRoom room = gameRoomDao.findByCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));

        List<GameParticipant> participants =
                gameParticipantDao.findByGameRoomIdOrderByTotalScoreDesc(room.getId());

        List<LeaderboardEntryDto> leaderboard = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            GameParticipant p = participants.get(i);
            int correctAnswers = playerAnswerDao.countByParticipantIdAndCorrectTrue(p.getId());
            leaderboard.add(new LeaderboardEntryDto(i + 1, p.getUsername(), p.getTotalScore(), correctAnswers));
        }
        return leaderboard;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameResultDto> getPlayerHistory(Long userId) {
        return gameResultDao.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(gameResultTransformer::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> getGlobalTop10() {
        List<GameResult> sortedResults = gameResultDao.findAllByOrderByFinalScoreDescCreatedAtAsc();
        List<LeaderboardEntryDto> top10 = new ArrayList<>();
        Set<Long> addedUsers = new HashSet<>();

        for (GameResult result : sortedResults) {
            if (addedUsers.contains(result.getUserId())) {
                continue;
            }
            addedUsers.add(result.getUserId());
            top10.add(new LeaderboardEntryDto(
                    top10.size() + 1,
                    result.getUsername(),
                    result.getFinalScore(),
                    result.getCorrectAnswers()
            ));
            if (top10.size() == 10) {
                break;
            }
        }
        return top10;
    }

    public void saveGameResults(String roomCode, int totalQuestions) {
        GameRoom room = gameRoomDao.findByCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));

        List<GameParticipant> participants =
                gameParticipantDao.findByGameRoomIdOrderByTotalScoreDesc(room.getId());

        for (int i = 0; i < participants.size(); i++) {
            GameParticipant p = participants.get(i);
            int correctAnswers = playerAnswerDao.countByParticipantIdAndCorrectTrue(p.getId());

            GameResult result = new GameResult();
            result.setGameRoomId(room.getId());
            result.setUserId(p.getUserId());
            result.setUsername(p.getUsername());
            result.setFinalScore(p.getTotalScore());
            result.setRank(i + 1);
            result.setCorrectAnswers(correctAnswers);
            result.setTotalQuestions(totalQuestions);
            gameResultDao.save(result);
        }
    }
}
