package com.Karyakina.Ustenko.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Karyakina.Ustenko.dao.GameParticipantDao;
import com.Karyakina.Ustenko.dao.GameRoomDao;
import com.Karyakina.Ustenko.dto.CreateRoomDto;
import com.Karyakina.Ustenko.dto.GameRoomDto;
import com.Karyakina.Ustenko.dto.QuizInfoDto;
import com.Karyakina.Ustenko.model.GameParticipant;
import com.Karyakina.Ustenko.model.GameRoom;
import com.Karyakina.Ustenko.model.GameStatus;
import com.Karyakina.Ustenko.service.GameRoomService;
import com.Karyakina.Ustenko.service.QuizClient;
import com.Karyakina.Ustenko.transformer.GameRoomTransformer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class GameRoomServiceImpl implements GameRoomService {

    private final GameRoomDao gameRoomDao;
    private final GameParticipantDao gameParticipantDao;
    private final GameRoomTransformer gameRoomTransformer;
    private final QuizClient quizClient;

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    @Override
    public GameRoomDto createRoom(CreateRoomDto dto, Long hostUserId, String username) {
        GameRoom room = new GameRoom();
        room.setCode(generateUniqueCode());
        room.setQuizId(dto.getQuizId());
        room.setHostUserId(hostUserId);
        room.setStatus(GameStatus.WAITING);

        GameRoom savedRoom = gameRoomDao.save(room);

        GameParticipant host = new GameParticipant();
        host.setGameRoom(savedRoom);
        host.setUserId(hostUserId);
        host.setUsername(username);
        gameParticipantDao.save(host);
        savedRoom.getParticipants().add(host);

        return gameRoomTransformer.toDto(savedRoom);
    }

    @Override
    public GameRoomDto joinRoom(String roomCode, Long userId, String username) {
        GameRoom room = findRoomByCode(roomCode);

        if (room.getStatus() != GameStatus.WAITING) {
            throw new IllegalStateException("Комната уже не принимает игроков");
        }

        if (gameParticipantDao.existsByGameRoomIdAndUserId(room.getId(), userId)) {
            GameParticipant existing = gameParticipantDao
                    .findByGameRoomIdAndUserId(room.getId(), userId)
                    .orElseThrow();
            existing.setConnected(true);
            gameParticipantDao.save(existing);
        } else {
            GameParticipant participant = new GameParticipant();
            participant.setGameRoom(room);
            participant.setUserId(userId);
            participant.setUsername(username);
            gameParticipantDao.save(participant);
            room.getParticipants().add(participant);
        }

        return gameRoomTransformer.toDto(gameRoomDao.save(room));
    }

    @Override
    @Transactional(readOnly = true)
    public GameRoomDto findByCode(String code) {
        return gameRoomTransformer.toDto(findRoomByCode(code));
    }

    @Override
    public GameRoomDto startGame(String roomCode, Long hostUserId, String authToken) {
        GameRoom room = findRoomByCode(roomCode);
        checkHost(room, hostUserId);

        QuizInfoDto quiz = quizClient.getQuiz(room.getQuizId(), authToken);
        if (quiz == null || quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            throw new IllegalStateException("Квиз не содержит вопросов");
        }

        room.setStatus(GameStatus.IN_PROGRESS);
        room.setStartedAt(LocalDateTime.now());
        room.setCurrentQuestionIndex(0);
        return gameRoomTransformer.toDto(gameRoomDao.save(room));
    }

    @Override
    public GameRoomDto nextQuestion(String roomCode, Long hostUserId) {
        GameRoom room = findRoomByCode(roomCode);
        checkHost(room, hostUserId);
        room.setCurrentQuestionIndex(room.getCurrentQuestionIndex() + 1);
        return gameRoomTransformer.toDto(gameRoomDao.save(room));
    }

    @Override
    public GameRoomDto finishGame(String roomCode, Long hostUserId) {
        GameRoom room = findRoomByCode(roomCode);
        checkHost(room, hostUserId);
        room.setStatus(GameStatus.FINISHED);
        room.setFinishedAt(LocalDateTime.now());
        return gameRoomTransformer.toDto(gameRoomDao.save(room));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameRoomDto> findRoomsByHost(Long hostUserId) {
        return gameRoomDao.findByHostUserId(hostUserId).stream()
                .map(gameRoomTransformer::toDto)
                .toList();
    }

    private GameRoom findRoomByCode(String code) {
        return gameRoomDao.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена: " + code));
    }

    private void checkHost(GameRoom room, Long userId) {
        if (!room.getHostUserId().equals(userId)) {
            throw new AccessDeniedException("Только хост может управлять комнатой");
        }
    }

    private String generateUniqueCode() {
        Random random = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (gameRoomDao.findByCode(code).isPresent());
        return code;
    }
}
