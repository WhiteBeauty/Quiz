package com.Karyakina.Ustenko.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class SessionConfig {

    public static final String TOKEN_KEY = "jwt_token";
    public static final String USER_KEY = "current_user";

    public String getToken(HttpSession session) {
        return (String) session.getAttribute(TOKEN_KEY);
    }

    public boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(TOKEN_KEY) != null;
    }
}
