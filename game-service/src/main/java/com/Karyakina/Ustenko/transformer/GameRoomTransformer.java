package com.Karyakina.Ustenko.transformer;

import org.springframework.stereotype.Component;
import com.Karyakina.Ustenko.dto.GameRoomDto;
import com.Karyakina.Ustenko.dto.ParticipantDto;
import com.Karyakina.Ustenko.model.GameParticipant;
import com.Karyakina.Ustenko.model.GameRoom;

@Component
public class GameRoomTransformer {

    public GameRoomDto toDto(GameRoom room) {
        GameRoomDto dto = new GameRoomDto();
        dto.setId(room.getId());
        dto.setCode(room.getCode());
        dto.setQuizId(room.getQuizId());
        dto.setHostUserId(room.getHostUserId());
        dto.setStatus(room.getStatus().name());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setCurrentQuestionIndex(room.getCurrentQuestionIndex());
        dto.setParticipants(room.getParticipants().stream()
                .map(this::toParticipantDto)
                .toList());
        return dto;
    }

    public ParticipantDto toParticipantDto(GameParticipant participant) {
        ParticipantDto dto = new ParticipantDto();
        dto.setId(participant.getId());
        dto.setUserId(participant.getUserId());
        dto.setUsername(participant.getUsername());
        dto.setTotalScore(participant.getTotalScore());
        dto.setConnected(participant.isConnected());
        return dto;
    }
}
