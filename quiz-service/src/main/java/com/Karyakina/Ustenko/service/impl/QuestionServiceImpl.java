package com.Karyakina.Ustenko.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Karyakina.Ustenko.dao.QuestionDao;
import com.Karyakina.Ustenko.dao.QuizDao;
import com.Karyakina.Ustenko.dao.UserDao;
import com.Karyakina.Ustenko.dto.QuestionCreateDto;
import com.Karyakina.Ustenko.dto.QuestionDto;
import com.Karyakina.Ustenko.model.Question;
import com.Karyakina.Ustenko.model.Quiz;
import com.Karyakina.Ustenko.service.QuestionService;
import com.Karyakina.Ustenko.transformer.QuestionTransformer;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuestionDao questionDao;
    private final QuizDao quizDao;
    private final UserDao userDao;
    private final QuestionTransformer questionTransformer;

    @Override
    public QuestionDto addQuestion(Long quizId, QuestionCreateDto dto, String authorEmail) {
        Quiz quiz = findQuizById(quizId);
        checkOwnership(quiz, authorEmail);
        Question question = questionTransformer.fromCreateDto(dto, quiz);
        quiz.getQuestions().add(question);
        return questionTransformer.toDto(questionDao.save(question));
    }

    @Override
    public QuestionDto updateQuestion(Long questionId, QuestionCreateDto dto, String authorEmail) {
        Question question = findQuestionById(questionId);
        checkOwnership(question.getQuiz(), authorEmail);
        question.setText(dto.getText());
        question.setTimeLimitSeconds(dto.getTimeLimitSeconds());
        question.setPoints(dto.getPoints());
        question.setOrderIndex(dto.getOrderIndex());
        question.getAnswerOptions().clear();
        dto.getAnswerOptions().forEach(optDto -> {
            var option = new com.Karyakina.Ustenko.model.AnswerOption();
            option.setQuestion(question);
            option.setText(optDto.getText());
            option.setCorrect(optDto.isCorrect());
            option.setOrderIndex(optDto.getOrderIndex());
            question.getAnswerOptions().add(option);
        });
        return questionTransformer.toDto(questionDao.save(question));
    }

    @Override
    public void deleteQuestion(Long questionId, String authorEmail) {
        Question question = findQuestionById(questionId);
        checkOwnership(question.getQuiz(), authorEmail);
        questionDao.delete(question);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDto> findByQuiz(Long quizId) {
        return questionDao.findByQuizIdOrderByOrderIndex(quizId).stream()
                .map(questionTransformer::toDto)
                .toList();
    }

    private Quiz findQuizById(Long id) {
        return quizDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Квиз не найден"));
    }

    private Question findQuestionById(Long id) {
        return questionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Вопрос не найден"));
    }

    private void checkOwnership(Quiz quiz, String email) {
        if (!quiz.getAuthor().getEmail().equals(email)) {
            throw new AccessDeniedException("Нет прав на изменение этого квиза");
        }
    }
}
