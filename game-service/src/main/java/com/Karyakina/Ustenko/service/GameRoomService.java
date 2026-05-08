package com.Karyakina.Ustenko.service;

import com.Karyakina.Ustenko.dto.CreateRoomDto;
import com.Karyakina.Ustenko.dto.GameRoomDto;

import java.util.List;

public interface GameRoomService {
    GameRoomDto createRoom(CreateRoomDto dto, Long hostUserId, String username);
    GameRoomDto joinRoom(String roomCode, Long userId, String username);
    GameRoomDto findByCode(String code);
    GameRoomDto startGame(String roomCode, Long hostUserId, String authToken);
    GameRoomDto nextQuestion(String roomCode, Long hostUserId);
    GameRoomDto finishGame(String roomCode, Long hostUserId);
    List<GameRoomDto> findRoomsByHost(Long hostUserId);
}
