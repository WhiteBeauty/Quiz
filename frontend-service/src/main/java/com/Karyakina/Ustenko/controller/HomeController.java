package com.Karyakina.Ustenko.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.Karyakina.Ustenko.client.GameServiceClient;
import com.Karyakina.Ustenko.client.QuizServiceClient;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final QuizServiceClient quizServiceClient;
    private final GameServiceClient gameServiceClient;

    @Value("${frontend.quiz-url:http://localhost:8080}")
    private String quizUrl;

    @Value("${frontend.game-url:http://localhost:8081}")
    private String gameUrl;

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpServletRequest request) {
        model.addAttribute("quizServiceUrl", quizUrl);
        model.addAttribute("gameServiceUrl", gameUrl);
        String token = extractToken(request);
        boolean isAuthenticated = false;
        String currentUsername = null;
        if (token != null) {
            try {
                var currentUser = quizServiceClient.getCurrentUser(token);
                if (currentUser != null) {
                    isAuthenticated = true;
                    currentUsername = currentUser.getUsername();
                }
            } catch (Exception ignored) {
                isAuthenticated = false;
            }
        }
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("currentUsername", currentUsername);
    }

    @GetMapping("/")
    public String home(Model model) {
        try {
            model.addAttribute("quizzes", quizServiceClient.getPublishedQuizzes());
            model.addAttribute("leaderboard", gameServiceClient.getGlobalTop10());
        } catch (Exception e) {
            model.addAttribute("error", "Не удалось загрузить данные");
        }
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        String token = extractToken(request);
        if (token == null) {
            return "redirect:/auth/login";
        }
        try {
            model.addAttribute("user", quizServiceClient.getCurrentUser(token));
            model.addAttribute("myQuizzes", quizServiceClient.getMyQuizzes(token));
            model.addAttribute("myRooms", gameServiceClient.getMyRooms(token));
        } catch (Exception e) {
            if (isUnauthorizedError(e)) {
                return "redirect:/auth/login";
            }
            model.addAttribute("error", "Не удалось загрузить часть данных кабинета. Попробуйте обновить страницу.");
            try {
                model.addAttribute("user", quizServiceClient.getCurrentUser(token));
            } catch (Exception ignored) {
                return "redirect:/auth/login";
            }
            model.addAttribute("myQuizzes", java.util.List.of());
            model.addAttribute("myRooms", java.util.List.of());
        }
        return "dashboard";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        try {
            model.addAttribute("top10", gameServiceClient.getGlobalTop10());
        } catch (Exception e) {
            model.addAttribute("error", "Не удалось загрузить таблицу лидеров");
        }
        return "leaderboard";
    }

    String extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (var cookie : request.getCookies()) {
            if ("JWT_TOKEN".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private boolean isUnauthorizedError(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof WebClientResponseException ex) {
                int status = ex.getStatusCode().value();
                return status == 401 || status == 403;
            }
            current = current.getCause();
        }
        return false;
    }
}