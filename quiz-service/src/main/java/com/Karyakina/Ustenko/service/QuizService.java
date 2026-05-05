package com.Karyakina.Ustenko.service;

import com.Karyakina.Ustenko.dto.QuizCreateDto;
import com.Karyakina.Ustenko.dto.QuizDto;

import java.util.List;

public interface QuizService {
    QuizDto create(QuizCreateDto dto, String authorEmail);
    QuizDto findById(Long id);
    List<QuizDto> findByAuthor(String authorEmail);
    List<QuizDto> findAllPublished();
    List<QuizDto> search(String keyword);
    QuizDto publish(Long id, String authorEmail);
    QuizDto update(Long id, QuizCreateDto dto, String authorEmail);
    void delete(Long id, String authorEmail);
}
