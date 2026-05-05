package com.Karyakina.Ustenko.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Karyakina.Ustenko.config.JwtService;
import com.Karyakina.Ustenko.dao.UserDao;
import com.Karyakina.Ustenko.dto.AuthResponseDto;
import com.Karyakina.Ustenko.dto.LoginRequestDto;
import com.Karyakina.Ustenko.dto.RegisterRequestDto;
import com.Karyakina.Ustenko.dto.UserDto;
import com.Karyakina.Ustenko.model.User;
import com.Karyakina.Ustenko.service.UserService;
import com.Karyakina.Ustenko.transformer.UserTransformer;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserTransformer userTransformer;

    @Override
    public AuthResponseDto register(RegisterRequestDto dto) {
        if (userDao.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
        if (userDao.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        User savedUser = userDao.save(user);
        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getId());
        return new AuthResponseDto(token, userTransformer.toDto(savedUser));
    }

    @Override
    public AuthResponseDto login(LoginRequestDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        User user = userDao.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        String token = jwtService.generateToken(user.getEmail(), user.getId());
        return new AuthResponseDto(token, userTransformer.toDto(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return userTransformer.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String email) {
        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return userTransformer.toDto(user);
    }
}
