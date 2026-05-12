package com.Karyakina.Ustenko.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class QuizDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Long authorId;
    private String authorUsername;
    private List<QuestionDto> questions;
    private LocalDateTime createdAt;
    private boolean published;
    private int questionCount;
}
