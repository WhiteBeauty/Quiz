package com.Karyakina.Ustenko.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Karyakina.Ustenko.model.Question;

import java.util.List;

public interface QuestionDao extends JpaRepository<Question, Long> {
    List<Question> findByQuizIdOrderByOrderIndex(Long quizId);
    void deleteByQuizId(Long quizId);
}
