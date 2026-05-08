package com.Karyakina.Ustenko.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Karyakina.Ustenko.model.GameResult;

import java.util.List;

public interface GameResultDao extends JpaRepository<GameResult, Long> {
    List<GameResult> findByGameRoomIdOrderByRank(Long gameRoomId);
    List<GameResult> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<GameResult> findTop10ByOrderByFinalScoreDesc();
    List<GameResult> findAllByOrderByFinalScoreDescCreatedAtAsc();
}
