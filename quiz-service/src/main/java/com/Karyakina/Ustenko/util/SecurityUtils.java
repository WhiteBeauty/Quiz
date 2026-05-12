package com.Karyakina.Ustenko.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long requireUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Требуется авторизация");
        }
        Object details = authentication.getDetails();
        if (details instanceof Long id) {
            return id;
        }
        throw new IllegalStateException("Не удалось определить пользователя");
    }

    public static Long currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object details = authentication.getDetails();
        return details instanceof Long id ? id : null;
    }

    public static String requireEmail(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails ud)) {
            throw new AccessDeniedException("Требуется авторизация");
        }
        return ud.getUsername();
    }
}
