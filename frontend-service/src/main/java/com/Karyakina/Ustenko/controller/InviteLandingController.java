package com.Karyakina.Ustenko.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.Karyakina.Ustenko.client.GameServiceClient;
import com.Karyakina.Ustenko.client.QuizServiceClient;
import com.Karyakina.Ustenko.dto.GameRoomDto;
import com.Karyakina.Ustenko.dto.social.GameInviteResultDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class InviteLandingController {

    private final HomeController homeController;
    private final QuizServiceClient quizServiceClient;
    private final GameServiceClient gameServiceClient;

    @GetMapping("/invite/game")
    public String consumeInvite(@RequestParam(required = false) String t, HttpServletRequest request) {
        if (t == null || t.isBlank()) {
            return "redirect:/friends";
        }
        String jwt = homeController.extractToken(request);
        if (jwt == null) {
            String redirect = "/invite/game?t=" + URLEncoder.encode(t, StandardCharsets.UTF_8);
            return "redirect:/auth/login?redirect=" + URLEncoder.encode(redirect, StandardCharsets.UTF_8);
        }
        try {
            GameInviteResultDto r = quizServiceClient.consumeGameInvite(t.trim(), jwt);
            if ("JOIN_ROOM".equals(r.getAction())) {
                String code = r.getRoomCode() == null ? "" : r.getRoomCode().trim().toUpperCase();
                gameServiceClient.joinRoom(code, jwt);
                return "redirect:/game/room/" + code;
            }
            if (r.getQuizId() == null) {
                return "redirect:/friends?error=invite";
            }
            GameRoomDto room = gameServiceClient.createRoom(r.getQuizId(), jwt);
            return "redirect:/game/room/" + room.getCode();
        } catch (Exception e) {
            return "redirect:/friends?error=invite";
        }
    }
}
