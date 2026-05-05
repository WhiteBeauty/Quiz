package com.Karyakina.Ustenko.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Karyakina.Ustenko.dao.QuizDao;
import com.Karyakina.Ustenko.dao.UserDao;
import com.Karyakina.Ustenko.dto.QuizCreateDto;
import com.Karyakina.Ustenko.dto.QuizDto;
import com.Karyakina.Ustenko.model.Quiz;
import com.Karyakina.Ustenko.model.User;
import com.Karyakina.Ustenko.service.QuizService;
import com.Karyakina.Ustenko.transformer.QuizTransformer;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {

    private final QuizDao quizDao;
    private final UserDao userDao;
    private final QuizTransformer quizTransformer;

    @Override
    public QuizDto create(QuizCreateDto dto, String authorEmail) {
        User author = findUserByEmail(authorEmail);
        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setCategory(dto.getCategory());
        quiz.setAuthor(author);
        return quizTransformer.toDto(quizDao.save(quiz));
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDto findById(Long id) {
        return quizTransformer.toDto(findQuizById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDto> findByAuthor(String authorEmail) {
        User author = findUserByEmail(authorEmail);
        return quizDao.findByAuthorId(author.getId()).stream()
                .map(quizTransformer::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDto> findAllPublished() {
        return quizDao.findByPublishedTrue().stream()
                .map(quizTransformer::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDto> search(String keyword) {
        return quizDao.searchPublished(keyword).stream()
                .map(quizTransformer::toDto)
                .toList();
    }

    @Override
    public QuizDto publish(Long id, String authorEmail) {
        Quiz quiz = findQuizById(id);
        checkOwnership(quiz, authorEmail);
        if (quiz.getQuestions().isEmpty()) {
            throw new IllegalStateException("Нельзя опубликовать квиз без вопросов");
        }
        quiz.setPublished(true);
        return quizTransformer.toDto(quizDao.save(quiz));
    }

    @Override
    public QuizDto update(Long id, QuizCreateDto dto, String authorEmail) {
        Quiz quiz = findQuizById(id);
        checkOwnership(quiz, authorEmail);
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setCategory(dto.getCategory());
        return quizTransformer.toDto(quizDao.save(quiz));
    }

    @Override
    public void delete(Long id, String authorEmail) {
        Quiz quiz = findQuizById(id);
        checkOwnership(quiz, authorEmail);
        quizDao.delete(quiz);
    }

    private Quiz findQuizById(Long id) {
        return quizDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Квиз не найден"));
    }

    private User findUserByEmail(String email) {
        return userDao.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    private void checkOwnership(Quiz quiz, String email) {
        if (!quiz.getAuthor().getEmail().equals(email)) {
            throw new AccessDeniedException("Нет прав на изменение этого квиза");
        }
    }
}
