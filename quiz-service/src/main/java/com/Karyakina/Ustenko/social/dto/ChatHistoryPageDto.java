package com.Karyakina.Ustenko.social.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatHistoryPageDto {
    private List<ChatMessageDto> messages;
    private boolean hasMore;
}
