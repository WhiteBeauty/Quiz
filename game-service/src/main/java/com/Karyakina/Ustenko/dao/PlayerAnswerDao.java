package com.Karyakina.Ustenko.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Karyakina.Ustenko.model.PlayerAnswer;

import java.util.List;
import java.util.Optional;

public interface PlayerAnswerDao extends JpaRepository<PlayerAnswer, Long> {
    Optional<PlayerAnswer> findByGameRoomIdAndParticipantIdAndQuestionId(
            Long gameRoomId, Long participantId, Long questionId);
    List<PlayerAnswer> findByGameRoomIdAndQuestionId(Long gameRoomId, Long questionId);
    List<PlayerAnswer> findByParticipantId(Long participantId);
    int countByParticipantIdAndCorrectTrue(Long participantId);
    long countByGameRoomIdAndQuestionId(Long gameRoomId, Long questionId);
}
