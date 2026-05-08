package com.Karyakina.Ustenko;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.Karyakina.Ustenko.dao.GameParticipantDao;
import com.Karyakina.Ustenko.dao.GameResultDao;
import com.Karyakina.Ustenko.dao.GameRoomDao;
import com.Karyakina.Ustenko.dao.PlayerAnswerDao;
import com.Karyakina.Ustenko.dto.LeaderboardEntryDto;
import com.Karyakina.Ustenko.model.GameResult;
import com.Karyakina.Ustenko.service.impl.LeaderboardServiceImpl;
import com.Karyakina.Ustenko.transformer.GameResultTransformer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private GameRoomDao gameRoomDao;
    @Mock
    private GameParticipantDao gameParticipantDao;
    @Mock
    private PlayerAnswerDao playerAnswerDao;
    @Mock
    private GameResultDao gameResultDao;
    @Mock
    private GameResultTransformer gameResultTransformer;

    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;

    @Test
    void getGlobalTop10_shouldReturnBestResultPerPlayerOnly() {
        GameResult aliceBest = gameResult(1L, "alice", 1000, 9);
        GameResult aliceOld = gameResult(1L, "alice", 800, 7);
        GameResult bobBest = gameResult(2L, "bob", 900, 8);

        when(gameResultDao.findAllByOrderByFinalScoreDescCreatedAtAsc())
                .thenReturn(List.of(aliceBest, bobBest, aliceOld));

        List<LeaderboardEntryDto> result = leaderboardService.getGlobalTop10();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("alice");
        assertThat(result.get(0).getScore()).isEqualTo(1000);
        assertThat(result.get(0).getRank()).isEqualTo(1);
        assertThat(result.get(1).getUsername()).isEqualTo("bob");
        assertThat(result.get(1).getScore()).isEqualTo(900);
        assertThat(result.get(1).getRank()).isEqualTo(2);
    }

    private GameResult gameResult(Long userId, String username, int score, int correctAnswers) {
        GameResult result = new GameResult();
        result.setUserId(userId);
        result.setUsername(username);
        result.setFinalScore(score);
        result.setCorrectAnswers(correctAnswers);
        return result;
    }
}
