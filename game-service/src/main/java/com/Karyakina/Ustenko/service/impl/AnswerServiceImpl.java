package com.Karyakina.Ustenko.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Karyakina.Ustenko.dao.GameParticipantDao;
import com.Karyakina.Ustenko.dao.GameRoomDao;
import com.Karyakina.Ustenko.dao.PlayerAnswerDao;
import com.Karyakina.Ustenko.dto.QuizInfoDto;
import com.Karyakina.Ustenko.dto.QuizQuestionDto;
import com.Karyakina.Ustenko.dto.SubmitAnswerDto;
import com.Karyakina.Ustenko.model.GameParticipant;
import com.Karyakina.Ustenko.model.GameRoom;
import com.Karyakina.Ustenko.model.GameStatus;
import com.Karyakina.Ustenko.model.PlayerAnswer;
import com.Karyakina.Ustenko.service.AnswerService;
import com.Karyakina.Ustenko.service.QuizClient;

@Service
@RequiredArgsConstructor
@Transactional
public class AnswerServiceImpl implements AnswerService {

    private final PlayerAnswerDao playerAnswerDao;
    private final GameRoomDao gameRoomDao;
    private final GameParticipantDao gameParticipantDao;
    private final QuizClient quizClient;

    @Override
    public PlayerAnswer submitAnswer(String roomCode, Long userId, SubmitAnswerDto dto, String authToken) {
        GameRoom room = gameRoomDao.findByCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));

        if (room.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Игра не активна");
        }

        GameParticipant participant = gameParticipantDao
                .findByGameRoomIdAndUserId(room.getId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Вы не участник этой игры"));

        if (playerAnswerDao.findByGameRoomIdAndParticipantIdAndQuestionId(
                room.getId(), participant.getId(), dto.getQuestionId()).isPresent()) {
            throw new IllegalStateException("Вы уже ответили на этот вопрос");
        }

        QuizInfoDto quiz = quizClient.getQuiz(room.getQuizId(), authToken);
        QuizQuestionDto currentQuestion = quiz.getQuestions().stream()
                .filter(q -> q.getId().equals(dto.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Вопрос не найден"));

        boolean isCorrect = currentQuestion.getAnswerOptions().stream()
                .filter(opt -> opt.getId().equals(dto.getSelectedOptionId()))
                .findFirst()
                .map(opt -> opt.isCorrect())
                .orElse(false);

        int pointsEarned = 0;
        if (isCorrect) {
            double timeRatio = 1.0 - ((double) dto.getResponseTimeMs() /
                    (currentQuestion.getTimeLimitSeconds() * 1000L));
            pointsEarned = (int) (currentQuestion.getPoints() * Math.max(0.1, timeRatio));
            participant.setTotalScore(participant.getTotalScore() + pointsEarned);
            gameParticipantDao.save(participant);
        }

        PlayerAnswer answer = new PlayerAnswer();
        answer.setGameRoomId(room.getId());
        answer.setParticipantId(participant.getId());
        answer.setQuestionId(dto.getQuestionId());
        answer.setSelectedOptionId(dto.getSelectedOptionId());
        answer.setCorrect(isCorrect);
        answer.setPointsEarned(pointsEarned);
        answer.setResponseTimeMs(dto.getResponseTimeMs());
        return playerAnswerDao.save(answer);
    }
}
