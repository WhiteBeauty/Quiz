package com.Karyakina.Ustenko.transformer;

import org.springframework.stereotype.Component;
import com.Karyakina.Ustenko.dto.UserDto;
import com.Karyakina.Ustenko.model.User;

@Component
public class UserTransformer {

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole().name());
        return dto;
    }
}
