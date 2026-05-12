package com.Karyakina.Ustenko.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.Karyakina.Ustenko.client.QuizServiceClient;
import com.Karyakina.Ustenko.dto.AuthResponseDto;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final QuizServiceClient quizServiceClient;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String registered,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверный email или пароль");
        }
        if (registered != null) {
            model.addAttribute("success", "Регистрация успешна! Войдите в аккаунт");
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletResponse response) {
        try {
            AuthResponseDto auth = quizServiceClient.login(email, password);
            addTokenCookie(response, auth.getToken());
            return "redirect:/dashboard";
        } catch (Exception e) {
            return "redirect:/auth/login?error=true";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String username,
                           @RequestParam String password,
                           Model model) {
        try {
            quizServiceClient.register(email, username, password);
            return "redirect:/auth/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка регистрации: " + e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT_TOKEN", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/";
    }

    private void addTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("JWT_TOKEN", token);
        cookie.setMaxAge(86400);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}
