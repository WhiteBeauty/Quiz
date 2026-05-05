package com.Karyakina.Ustenko.service;

import com.Karyakina.Ustenko.dto.QuestionCreateDto;
import com.Karyakina.Ustenko.dto.QuestionDto;

import java.util.List;

public interface QuestionService {
    QuestionDto addQuestion(Long quizId, QuestionCreateDto dto, String authorEmail);
    QuestionDto updateQuestion(Long questionId, QuestionCreateDto dto, String authorEmail);
    void deleteQuestion(Long questionId, String authorEmail);
    List<QuestionDto> findByQuiz(Long quizId);
}
