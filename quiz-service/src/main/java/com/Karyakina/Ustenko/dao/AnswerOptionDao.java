package com.Karyakina.Ustenko.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Karyakina.Ustenko.model.AnswerOption;

import java.util.List;

public interface AnswerOptionDao extends JpaRepository<AnswerOption, Long> {
    List<AnswerOption> findByQuestionIdOrderByOrderIndex(Long questionId);
}
