package com.Karyakina.Ustenko;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.Karyakina.Ustenko.dao.GameParticipantDao;
import com.Karyakina.Ustenko.dao.GameRoomDao;
import com.Karyakina.Ustenko.dto.CreateRoomDto;
import com.Karyakina.Ustenko.dto.GameRoomDto;
import com.Karyakina.Ustenko.model.GameRoom;
import com.Karyakina.Ustenko.model.GameStatus;
import com.Karyakina.Ustenko.service.QuizClient;
import com.Karyakina.Ustenko.service.impl.GameRoomServiceImpl;
import com.Karyakina.Ustenko.transformer.GameRoomTransformer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameRoomServiceTest {

    @Mock
    private GameRoomDao gameRoomDao;

    @Mock
    private GameParticipantDao gameParticipantDao;

    @Mock
    private GameRoomTransformer gameRoomTransformer;

    @Mock
    private QuizClient quizClient;

    @InjectMocks
    private GameRoomServiceImpl gameRoomService;

    private GameRoom testRoom;

    @BeforeEach
    void setUp() {
        testRoom = new GameRoom();
        testRoom.setId(1L);
        testRoom.setCode("ABCDEF");
        testRoom.setQuizId(1L);
        testRoom.setHostUserId(1L);
        testRoom.setStatus(GameStatus.WAITING);
    }

    @Test
    void createRoom_shouldGenerateCode() {
        CreateRoomDto dto = new CreateRoomDto();
        dto.setQuizId(1L);

        GameRoomDto expectedDto = new GameRoomDto();
        expectedDto.setCode("ABCDEF");

        when(gameRoomDao.findByCode(anyString())).thenReturn(Optional.empty());
        when(gameRoomDao.save(any(GameRoom.class))).thenReturn(testRoom);
        when(gameRoomTransformer.toDto(any())).thenReturn(expectedDto);

        GameRoomDto result = gameRoomService.createRoom(dto, 1L, "testuser");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isNotBlank();
    }

    @Test
    void joinRoom_withFinishedGame_shouldThrowException() {
        testRoom.setStatus(GameStatus.FINISHED);
        when(gameRoomDao.findByCode("ABCDEF")).thenReturn(Optional.of(testRoom));

        assertThatThrownBy(() -> gameRoomService.joinRoom("ABCDEF", 2L, "player2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Комната уже не принимает игроков");
    }

    @Test
    void findByCode_withNonExistingCode_shouldThrowException() {
        when(gameRoomDao.findByCode("XXXXXX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameRoomService.findByCode("XXXXXX"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
