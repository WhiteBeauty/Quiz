package com.Karyakina.Ustenko.social.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Karyakina.Ustenko.social.dao.ChatMessageDao;
import com.Karyakina.Ustenko.social.dto.ChatHistoryPageDto;
import com.Karyakina.Ustenko.social.dto.ChatMessageDto;
import com.Karyakina.Ustenko.social.dto.ChatSendDto;
import com.Karyakina.Ustenko.social.model.ChatMessage;
import com.Karyakina.Ustenko.social.redis.RedisRateLimiter;
import com.Karyakina.Ustenko.social.service.ChatService;
import com.Karyakina.Ustenko.social.service.FriendshipService;
import com.Karyakina.Ustenko.util.SecurityUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageDao chatMessageDao;
    private final FriendshipService friendshipService;
    private final RedisRateLimiter redisRateLimiter;

    @Override
    @Transactional
    public ChatHistoryPageDto getMessages(Long peerUserId, String beforeIso, int limit, Authentication authentication) {
        Long me = SecurityUtils.requireUserId(authentication);
        if (me.equals(peerUserId)) {
            throw new IllegalArgumentException("Нельзя открыть чат с самим собой");
        }
        if (!friendshipService.areFriends(me, peerUserId)) {
            throw new IllegalArgumentException("Чат доступен только друзьям");
        }
        int size = Math.min(Math.max(limit, 1), 100);
        LocalDateTime before = parseBefore(beforeIso);
        chatMessageDao.markDeliveredToMeFromPeer(me, peerUserId, LocalDateTime.now());
        Page<ChatMessage> page = chatMessageDao.findConversation(me, peerUserId, before, PageRequest.of(0, size));
        List<ChatMessage> chunk = page.getContent();
        List<ChatMessageDto> dtos = new ArrayList<>(chunk.size());
        for (int i = chunk.size() - 1; i >= 0; i--) {
            dtos.add(toDto(chunk.get(i)));
        }
        ChatHistoryPageDto out = new ChatHistoryPageDto();
        out.setMessages(dtos);
        out.setHasMore(chunk.size() == size);
        return out;
    }

    private static LocalDateTime parseBefore(String beforeIso) {
        if (beforeIso == null || beforeIso.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(beforeIso);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректный формат параметра before (ожидается ISO-8601)");
        }
    }

    @Override
    @Transactional
    public void sendMessage(Long peerUserId, ChatSendDto dto, Authentication authentication) {
        Long me = SecurityUtils.requireUserId(authentication);
        if (me.equals(peerUserId)) {
            throw new IllegalArgumentException("Нельзя отправить сообщение самому себе");
        }
        if (!friendshipService.areFriends(me, peerUserId)) {
            throw new IllegalArgumentException("Сообщения можно отправлять только друзьям");
        }
        redisRateLimiter.check(
                "rl:chat:msg:" + me,
                120,
                Duration.ofMinutes(1),
                "Слишком много сообщений. Сделайте паузу."
        );
        ChatMessage m = new ChatMessage();
        m.setSenderId(me);
        m.setRecipientId(peerUserId);
        m.setBody(dto.getText().trim());
        chatMessageDao.save(m);
    }

    @Override
    @Transactional
    public void markReadUpTo(Long peerUserId, Long upToMessageId, Authentication authentication) {
        Long me = SecurityUtils.requireUserId(authentication);
        if (!friendshipService.areFriends(me, peerUserId)) {
            throw new IllegalArgumentException("Недоступно");
        }
        if (upToMessageId == null || upToMessageId <= 0) {
            throw new IllegalArgumentException("Некорректный идентификатор сообщения");
        }
        chatMessageDao.markReadUpTo(me, peerUserId, upToMessageId, LocalDateTime.now());
    }

    private static ChatMessageDto toDto(ChatMessage m) {
        ChatMessageDto d = new ChatMessageDto();
        d.setId(m.getId());
        d.setSenderId(m.getSenderId());
        d.setRecipientId(m.getRecipientId());
        d.setBody(m.getBody());
        d.setCreatedAt(m.getCreatedAt());
        d.setDeliveredAt(m.getDeliveredAt());
        d.setReadAt(m.getReadAt());
        return d;
    }
}
