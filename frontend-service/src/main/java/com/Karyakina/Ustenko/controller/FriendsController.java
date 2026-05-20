package com.Karyakina.Ustenko.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.Karyakina.Ustenko.client.GameServiceClient;
import com.Karyakina.Ustenko.client.QuizServiceClient;
import com.Karyakina.Ustenko.dto.QuizDto;
import com.Karyakina.Ustenko.dto.social.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/friends")
@RequiredArgsConstructor
@Slf4j
public class FriendsController {

    private final QuizServiceClient quizServiceClient;
    private final GameServiceClient gameServiceClient;
    private final HomeController homeController;

    @GetMapping
    public String friendsPage(
            @RequestParam(required = false) String error,
            HttpServletRequest request,
            Model model) {
        String token = homeController.extractToken(request);
        if (token == null) {
            return "redirect:/auth/login?redirect=/friends";
        }
        try {
            model.addAttribute("user", quizServiceClient.getCurrentUser(token));
            model.addAttribute("friends", nullToEmpty(quizServiceClient.listFriends(token)));
            model.addAttribute("incoming", nullToEmpty(quizServiceClient.listIncomingFriendRequests(token)));
            model.addAttribute("outgoing", nullToEmpty(quizServiceClient.listOutgoingFriendRequests(token)));
            List<QuizDto> published = nullToEmpty(quizServiceClient.getPublishedQuizzes());
            List<QuizDto> mine = nullToEmpty(quizServiceClient.getMyQuizzes(token));
            Map<Long, QuizDto> byId = new LinkedHashMap<>();
            for (QuizDto q : published) {
                byId.putIfAbsent(q.getId(), q);
            }
            for (QuizDto q : mine) {
                byId.putIfAbsent(q.getId(), q);
            }
            model.addAttribute("quizzesForInvite", new ArrayList<>(byId.values()));
            model.addAttribute("myRooms", nullToEmpty(gameServiceClient.getMyRooms(token)));

            Map<Long, Long> unreadBySender = new HashMap<>();
            try {
                Map<Object, Object> rawUnread = quizServiceClient.getUnreadCountBySender(token);
                if (rawUnread != null && !rawUnread.isEmpty()) {
                    for (Map.Entry<Object, Object> entry : rawUnread.entrySet()) {
                        Long senderId = Long.valueOf(String.valueOf(entry.getKey()));
                        Long count = Long.valueOf(String.valueOf(entry.getValue()));
                        unreadBySender.put(senderId, count);
                    }
                }
            } catch (Exception e) {
                log.error("Error loading unread counts", e);
            }
            model.addAttribute("unreadBySender", unreadBySender);
            HttpSession session = request.getSession(false);
            if (session != null) {
                model.addAttribute("searchResults", session.getAttribute("friendSearchResults"));
                model.addAttribute("searchQuery", session.getAttribute("friendSearchQuery"));
                Object flash = session.getAttribute("lastInviteMessage");
                if (flash != null) {
                    model.addAttribute("inviteSuccess", flash);
                    session.removeAttribute("lastInviteMessage");
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", "Не удалось загрузить данные друзей");
            model.addAttribute("friends", List.of());
            model.addAttribute("incoming", List.of());
            model.addAttribute("outgoing", List.of());
            model.addAttribute("quizzesForInvite", List.of());
            model.addAttribute("myRooms", List.of());
            model.addAttribute("unreadBySender", new HashMap<Long, Long>());
        }
        if (error != null) {
            model.addAttribute("error", switch (error) {
                case "invite" -> "Не удалось отправить приглашение";
                case "search" -> "Ошибка поиска";
                case "request" -> "Не удалось отправить заявку";
                case "chat" -> "Чат недоступен (возможно, пользователь не в друзьях)";
                default -> "Операция не выполнена";
            });
        }
        return "social/friends";
    }

    @PostMapping("/search")
    public String search(@RequestParam String q, HttpServletRequest request) {
        String token = homeController.extractToken(request);
        if (token == null) {
            return "redirect:/auth/login?redirect=/friends";
        }
        try {
            List<UserSearchHitDto> hits = quizServiceClient.searchFriends(q, token);
            request.getSession().setAttribute("friendSearchResults", hits);
            request.getSession().setAttribute("friendSearchQuery", q);
        } catch (Exception e) {
            return "redirect:/friends?error=search";
        }
        return "redirect:/friends";
    }

    @PostMapping("/request")
    public String sendRequest(@RequestParam Long targetUserId, HttpServletRequest request) {
        String token = homeController.extractToken(request);
        if (token == null) {
            return "redirect:/auth/login?redirect=/friends";
        }
        try {
            quizServiceClient.sendFriendRequest(targetUserId, token);
        } catch (Exception e) {
            return "redirect:/friends?error=request";
        }
        return "redirect:/friends";
    }

    @PostMapping("/accept/{id}")
    public String accept(@PathVariable Long id, HttpServletRequest request) {
        String token = homeController.extractToken(request);
        if (token == null) {
            return "redirect:/auth/login?redirect=/friends";
        }
        try {
            quizServiceClient.acceptFriendRequest(id, token);
        } catch (Exception ignored) {
        }
        return "redirect:/friends";
    }

    @PostMapping("/decline/{id}")
    public String decline(@PathVariable Long id, HttpServletRequest request) {
        String token = homeController.extractToken(request);
        if (token == null) {
            return "redirect:/auth/login?redirect=/friends";
        }
        try {
            quizServiceClient.declineFriendRequest(id, token);
        } catch (Exception ignored) {
        }
        return "redirect:/friends";
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id, HttpServletRequest request) {
        String token = homeController.extractToken(request);
        if (token == null) {
            return "redirect:/auth/login?redirect=/friends";
        }
        try {
            quizServiceClient.cancelFriendRequest(id, token);
        } catch (Exception ignored) {
        }
        return "redirect:/friends";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long userId, HttpServletRequest request) {
        String token = homeController.extractToken(request);
        if (token == null) {
            return "redirect:/auth/login?redirect=/friends";
        }
        try {
            quizServiceClient.removeFriend(userId, token);
        } catch (Exception ignored) {
        }
        return "redirect:/friends";
    }

    @PostMapping("/invite")
    public String invite(
            @RequestParam Long friendUserId,
            @RequestParam String inviteType,
            @RequestParam(required = false) String roomCode,
            @RequestParam(required = false) Long quizId,
            HttpServletRequest request) {
        String token = homeController.extractToken(request);
        if (token == null) {
            return "redirect:/auth/login?redirect=/friends";
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("friendUserId", friendUserId);
            body.put("inviteType", inviteType);
            if ("JOIN_EXISTING".equals(inviteType) && roomCode != null) {
                body.put("roomCode", roomCode.trim());
            }
            if ("CREATE_NEW_GAME".equals(inviteType) && quizId != null) {
                body.put("quizId", quizId);
            }
            GameInviteSendResponseDto res = quizServiceClient.createGameInvite(token, body);
            request.getSession().setAttribute("lastInviteMessage", res.getMessage() + " " + res.getInviteLink());
        } catch (Exception e) {
            return "redirect:/friends?error=invite";
        }
        return "redirect:/friends";
    }

    private static <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static <K, V> Map<K, V> nullToEmptyMap(Map<K, V> map) {
        return map == null ? Map.of() : map;
    }
}
