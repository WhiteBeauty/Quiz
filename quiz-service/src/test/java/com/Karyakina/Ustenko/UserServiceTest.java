package com.Karyakina.Ustenko;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.Karyakina.Ustenko.config.JwtService;
import com.Karyakina.Ustenko.dao.UserDao;
import com.Karyakina.Ustenko.dto.RegisterRequestDto;
import com.Karyakina.Ustenko.model.User;
import com.Karyakina.Ustenko.service.impl.UserServiceImpl;
import com.Karyakina.Ustenko.transformer.UserTransformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserTransformer userTransformer;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_shouldRejectDuplicateEmail() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("taken@mail.com");
        dto.setUsername("newuser");
        dto.setPassword("secret");

        when(userDao.existsByEmail("taken@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Пользователь с таким email уже существует");
    }

    @Test
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("new@mail.com");
        dto.setUsername("newuser");
        dto.setPassword("secret");

        User saved = new User();
        saved.setId(10L);
        saved.setEmail("new@mail.com");
        saved.setUsername("newuser");

        when(userDao.existsByEmail("new@mail.com")).thenReturn(false);
        when(userDao.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userDao.save(any(User.class))).thenReturn(saved);
        when(jwtService.generateToken("new@mail.com", 10L)).thenReturn("jwt-token");
        when(userTransformer.toDto(saved)).thenReturn(new com.Karyakina.Ustenko.dto.UserDto());

        var response = userService.register(dto);

        assertThat(response.getToken()).isEqualTo("jwt-token");
    }
}
