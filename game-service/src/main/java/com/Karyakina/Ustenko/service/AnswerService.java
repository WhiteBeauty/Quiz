package com.Karyakina.Ustenko.service;

import com.Karyakina.Ustenko.dto.SubmitAnswerDto;
import com.Karyakina.Ustenko.model.PlayerAnswer;

public interface AnswerService {
    PlayerAnswer submitAnswer(String roomCode, Long userId, SubmitAnswerDto dto, String authToken);
}
