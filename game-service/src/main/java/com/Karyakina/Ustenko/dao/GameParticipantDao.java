package com.Karyakina.Ustenko.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Karyakina.Ustenko.model.GameParticipant;

import java.util.List;
import java.util.Optional;

public interface GameParticipantDao extends JpaRepository<GameParticipant, Long> {
    Optional<GameParticipant> findByGameRoomIdAndUserId(Long gameRoomId, Long userId);
    List<GameParticipant> findByGameRoomIdOrderByTotalScoreDesc(Long gameRoomId);
    boolean existsByGameRoomIdAndUserId(Long gameRoomId, Long userId);
    long countByGameRoomId(Long gameRoomId);
}
