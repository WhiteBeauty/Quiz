package com.Karyakina.Ustenko.social.service;

import org.springframework.security.core.Authentication;
import com.Karyakina.Ustenko.social.dto.ChatHistoryPageDto;
import com.Karyakina.Ustenko.social.dto.ChatSendDto;

import java.util.Map;

public interface ChatService {

    ChatHistoryPageDto getMessages(Long peerUserId, String beforeIso, int limit, Authentication authentication);

    void sendMessage(Long peerUserId, ChatSendDto dto, Authentication authentication);

    void markReadUpTo(Long peerUserId, Long upToMessageId, Authentication authentication);

    long getUnreadMessagesCount(Authentication authentication);

    Map<Long, Long> getUnreadCountBySender(Authentication authentication);
}
