package com.Karyakina.Ustenko.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.Karyakina.Ustenko.client.QuizServiceClient;
import com.Karyakina.Ustenko.dto.UserDto;
import com.Karyakina.Ustenko.dto.social.ChatHistoryPageDto;
import com.Karyakina.Ustenko.dto.social.ChatMessageDto;
import com.Karyakina.Ustenko.dto.social.FriendDto;

import java.util.List;

@Controller
@RequestMapping("/friends/chat")
@RequiredArgsConstructor
public class SocialChatController {

    private final QuizServiceClient quizServiceClient;
    private final HomeController homeController;

    @GetMapping("/{peerUserId}")
    public String chatPage(
            @PathVariable Long peerUserId,
            @RequestParam(required = false) String before,
            HttpServletRequest request,
            Model model) {
        String token = homeController.extractToken(request);
        if (token == null) {
            return "redirect:/auth/login?redirect=/friends/chat/" + peerUserId;
        }
        UserDto me;
        try {
            me = quizServiceClient.getCurrentUser(token);
            ChatHistoryPageDto page = quizServiceClient.getChatMessages(peerUserId, before, 40, token);
            model.addAttribute("messages", page.getMessages());
            model.addAttribute("hasMore", page.isHasMore());
            model.addAttribute("oldestCursor", page.getMessages().isEmpty() ? null
                    : page.getMessages().getFirst().getCreatedAt());
            String peerName = resolvePeerName(peerUserId, token);
            model.addAttribute("peerUserId", peerUserId);
            model.addAttribute("peerUsername", peerName);
            model.addAttribute("user", me);
            markReadIfNeeded(page.getMessages(), me.getId(), peerUserId, token);
        } catch (Exception e) {
            return "redirect:/friends?error=chat";
        }
        return "social/chat";
    }

    private void markReadIfNeeded(List<ChatMessageDto> messages, Long myId, Long peerId, String token) {
        Long maxIncoming = null;
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessageDto m = messages.get(i);
            if (m.getSenderId().equals(peerId) && m.getRecipientId().equals(myId)) {
                maxIncoming = m.getId();
                break;
            }
        }
        if (maxIncoming != null) {
            try {
                quizServiceClient.markChatRead(peerId, maxIncoming, token);
            } catch (Exception ignored) {
            }
        }
    }

    private String resolvePeerName(Long peerUserId, String token) {
        try {
            for (FriendDto f : nullToEmpty(quizServiceClient.listFriends(token))) {
                if (peerUserId.equals(f.getUserId())) {
                    return f.getUsername();
                }
            }
        } catch (Exception ignored) {
        }
        return "user-" + peerUserId;
    }

    @PostMapping("/{peerUserId}/send")
    public String send(
            @PathVariable Long peerUserId,
            @RequestParam String text,
            HttpServletRequest request) {
        String token = homeController.extractToken(request);
        if (token == null) {
            return "redirect:/auth/login?redirect=/friends/chat/" + peerUserId;
        }
        try {
            quizServiceClient.sendChatMessage(peerUserId, text, token);
        } catch (Exception ignored) {
        }
        return "redirect:/friends/chat/" + peerUserId;
    }

    private static <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }
}
