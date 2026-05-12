package com.Karyakina.Ustenko.dto;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class AuthDto {
    private String token;
    private UserDto user;
}
