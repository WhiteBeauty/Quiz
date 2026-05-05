package com.Karyakina.Ustenko.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.Karyakina.Ustenko.model.Quiz;

import java.util.List;

public interface QuizDao extends JpaRepository<Quiz, Long> {
    List<Quiz> findByAuthorId(Long authorId);
    List<Quiz> findByPublishedTrue();

    @Query("SELECT q FROM Quiz q WHERE q.published = true AND " +
            "(LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(q.category) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Quiz> searchPublished(@Param("keyword") String keyword);
}
