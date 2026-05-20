package com.Karyakina.Ustenko.controller;

import com.Karyakina.Ustenko.client.QuizServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final QuizServiceClient quizServiceClient;
    private final HomeController homeController;

    @ModelAttribute("unreadMessagesCount")
    public long unreadMessagesCount(HttpServletRequest request) {
        String token = homeController.extractToken(request);
        if (token == null) {
            return 0L;
        }
        try {
            Long count = quizServiceClient.getUnreadMessagesCount(token);
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}
