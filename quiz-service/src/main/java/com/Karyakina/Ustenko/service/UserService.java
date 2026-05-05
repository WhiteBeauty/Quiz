package com.Karyakina.Ustenko.service;

import com.Karyakina.Ustenko.dto.AuthResponseDto;
import com.Karyakina.Ustenko.dto.LoginRequestDto;
import com.Karyakina.Ustenko.dto.RegisterRequestDto;
import com.Karyakina.Ustenko.dto.UserDto;

public interface UserService {
    AuthResponseDto register(RegisterRequestDto dto);
    AuthResponseDto login(LoginRequestDto dto);
    UserDto findById(Long id);
    UserDto getCurrentUser(String email);
}
