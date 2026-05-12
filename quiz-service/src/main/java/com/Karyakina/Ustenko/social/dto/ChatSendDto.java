package com.Karyakina.Ustenko.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSendDto {

    @NotBlank
    @Size(max = 8000)
    private String text;
}
