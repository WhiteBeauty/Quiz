package com.Karyakina.Ustenko.transformer;

import org.springframework.stereotype.Component;
import com.Karyakina.Ustenko.dto.GameResultDto;
import com.Karyakina.Ustenko.model.GameResult;

@Component
public class GameResultTransformer {

    public GameResultDto toDto(GameResult result) {
        GameResultDto dto = new GameResultDto();
        dto.setId(result.getId());
        dto.setGameRoomId(result.getGameRoomId());
        dto.setUserId(result.getUserId());
        dto.setUsername(result.getUsername());
        dto.setFinalScore(result.getFinalScore());
        dto.setRank(result.getRank());
        dto.setCorrectAnswers(result.getCorrectAnswers());
        dto.setTotalQuestions(result.getTotalQuestions());
        dto.setCreatedAt(result.getCreatedAt());
        return dto;
    }
}
