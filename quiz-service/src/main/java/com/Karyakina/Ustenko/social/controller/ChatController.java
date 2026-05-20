package com.Karyakina.Ustenko.social.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.Karyakina.Ustenko.social.dto.ChatHistoryPageDto;
import com.Karyakina.Ustenko.social.dto.ChatSendDto;
import com.Karyakina.Ustenko.social.service.ChatService;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{peerUserId}/messages")
    public ResponseEntity<ChatHistoryPageDto> messages(
            @PathVariable Long peerUserId,
            @RequestParam(required = false) String before,
            @RequestParam(defaultValue = "30") int limit,
            Authentication authentication) {
        return ResponseEntity.ok(chatService.getMessages(peerUserId, before, limit, authentication));
    }

    @PostMapping("/{peerUserId}/messages")
    public ResponseEntity<Void> send(
            @PathVariable Long peerUserId,
            @Valid @RequestBody ChatSendDto dto,
            Authentication authentication) {
        chatService.sendMessage(peerUserId, dto, authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{peerUserId}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable Long peerUserId,
            @RequestParam Long upToMessageId,
            Authentication authentication) {
        chatService.markReadUpTo(peerUserId, upToMessageId, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount(Authentication authentication) {
        return ResponseEntity.ok(chatService.getUnreadMessagesCount(authentication));
    }

    @GetMapping("/unread-by-sender")
    public ResponseEntity<Map<Long, Long>> unreadBySender(Authentication authentication) {
        return ResponseEntity.ok(chatService.getUnreadCountBySender(authentication));
    }
}
