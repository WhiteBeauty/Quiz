package com.Karyakina.Ustenko.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Karyakina.Ustenko.model.GameRoom;
import com.Karyakina.Ustenko.model.GameStatus;

import java.util.List;
import java.util.Optional;

public interface GameRoomDao extends JpaRepository<GameRoom, Long> {
    Optional<GameRoom> findByCode(String code);
    List<GameRoom> findByHostUserId(Long hostUserId);
    List<GameRoom> findByStatus(GameStatus status);
}
