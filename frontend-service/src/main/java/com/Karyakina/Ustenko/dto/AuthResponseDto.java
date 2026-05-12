package com.Karyakina.Ustenko.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDto {
    private String token;
    private UserDto user;
}
