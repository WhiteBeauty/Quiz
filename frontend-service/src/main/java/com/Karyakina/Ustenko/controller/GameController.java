package com.Karyakina.Ustenko.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.Karyakina.Ustenko.client.GameServiceClient;
import com.Karyakina.Ustenko.client.QuizServiceClient;
import com.Karyakina.Ustenko.dto.GameRoomDto;

@Controller
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final GameServiceClient gameServiceClient;
    private final QuizServiceClient quizServiceClient;
    private final HomeController homeController;

    @GetMapping("/lobby")
    public String lobbyPage(HttpServletRequest request, Model model) {
        String token = homeController.extractToken(request);
        if (token == null) return "redirect:/auth/login";
        try {
            model.addAttribute("publishedQuizzes", quizServiceClient.getPublishedQuizzes());
        } catch (Exception e) {
            model.addAttribute("error", "Не удалось загрузить квизы");
        }
        return "game/lobby";
    }

    @PostMapping("/create")
    public String createRoom(@RequestParam Long quizId, HttpServletRequest request) {
        String token = homeController.extractToken(request);
        if (token == null) return "redirect:/auth/login";
        try {
            GameRoomDto room = gameServiceClient.createRoom(quizId, token);
            return "redirect:/game/room/" + room.getCode();
        } catch (Exception e) {
            return "redirect:/game/lobby?error=true";
        }
    }

    @PostMapping("/join")
    public String joinRoom(@RequestParam String roomCode, HttpServletRequest request) {
        String token = homeController.extractToken(request);
        if (token == null) return "redirect:/auth/login";
        try {
            gameServiceClient.joinRoom(roomCode.toUpperCase(), token);
            return "redirect:/game/room/" + roomCode.toUpperCase();
        } catch (Exception e) {
            return "redirect:/game/lobby?error=join";
        }
    }

    @GetMapping("/room/{code}")
    public String roomPage(@PathVariable String code,
                           HttpServletRequest request,
                           Model model) {
        String token = homeController.extractToken(request);
        if (token == null) return "redirect:/auth/login";
        try {
            GameRoomDto room = gameServiceClient.getRoom(code, token);
            var user = quizServiceClient.getCurrentUser(token);
            var quiz = quizServiceClient.getQuiz(room.getQuizId(), token);

            model.addAttribute("room", room);
            model.addAttribute("user", user);
            model.addAttribute("quiz", quiz);
            model.addAttribute("token", token);
            model.addAttribute("gameServiceUrl", "${game-service.ws-url}");
            model.addAttribute("isHost", room.getHostUserId().equals(user.getId()));
        } catch (Exception e) {
            return "redirect:/game/lobby?error=notfound";
        }
        return "game/room";
    }

    @GetMapping("/room/{code}/results")
    public String resultsPage(@PathVariable String code,
                              HttpServletRequest request,
                              Model model) {
        String token = homeController.extractToken(request);
        if (token == null) return "redirect:/auth/login";
        try {
            var leaderboard = gameServiceClient.getRoomLeaderboard(code, token);
            var room = gameServiceClient.getRoom(code, token);
            model.addAttribute("leaderboard", leaderboard);
            model.addAttribute("room", room);
        } catch (Exception e) {
            model.addAttribute("error", "Не удалось загрузить результаты");
        }
        return "game/results";
    }
}
