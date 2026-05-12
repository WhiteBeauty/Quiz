package com.Karyakina.Ustenko;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import com.Karyakina.Ustenko.client.GameServiceClient;
import com.Karyakina.Ustenko.client.QuizServiceClient;
import com.Karyakina.Ustenko.controller.HomeController;
import com.Karyakina.Ustenko.dto.UserDto;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private QuizServiceClient quizServiceClient;
    @Mock
    private GameServiceClient gameServiceClient;
    @Mock
    private Model model;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private HomeController homeController;

    @Test
    void addGlobalAttributes_shouldMarkAuthenticatedWhenTokenIsValid() {
        UserDto user = new UserDto();
        user.setUsername("alice");
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("JWT_TOKEN", "token-123")});
        when(quizServiceClient.getCurrentUser("token-123")).thenReturn(user);

        homeController.addGlobalAttributes(model, request);

        verify(model).addAttribute("isAuthenticated", true);
        verify(model).addAttribute("currentUsername", "alice");
    }

    @Test
    void addGlobalAttributes_shouldMarkGuestWhenTokenMissing() {
        when(request.getCookies()).thenReturn(null);

        homeController.addGlobalAttributes(model, request);

        verify(model).addAttribute("isAuthenticated", false);
        verify(model).addAttribute("currentUsername", null);
    }
}
