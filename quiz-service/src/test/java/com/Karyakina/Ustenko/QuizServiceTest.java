package com.Karyakina.Ustenko;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import com.Karyakina.Ustenko.dao.QuizDao;
import com.Karyakina.Ustenko.dao.UserDao;
import com.Karyakina.Ustenko.dto.QuizCreateDto;
import com.Karyakina.Ustenko.dto.QuizDto;
import com.Karyakina.Ustenko.model.Quiz;
import com.Karyakina.Ustenko.model.User;
import com.Karyakina.Ustenko.service.impl.QuizServiceImpl;
import com.Karyakina.Ustenko.transformer.QuizTransformer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizDao quizDao;

    @Mock
    private UserDao userDao;

    @Mock
    private QuizTransformer quizTransformer;

    @InjectMocks
    private QuizServiceImpl quizService;

    private User testUser;
    private Quiz testQuiz;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setUsername("testuser");

        testQuiz = new Quiz();
        testQuiz.setId(1L);
        testQuiz.setTitle("Тестовый квиз");
        testQuiz.setAuthor(testUser);
    }

    @Test
    void createQuiz_shouldCreateAndReturnDto() {
        QuizCreateDto createDto = new QuizCreateDto();
        createDto.setTitle("Тестовый квиз");

        QuizDto expectedDto = new QuizDto();
        expectedDto.setTitle("Тестовый квиз");

        when(userDao.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(quizDao.save(any(Quiz.class))).thenReturn(testQuiz);
        when(quizTransformer.toDto(testQuiz)).thenReturn(expectedDto);

        QuizDto result = quizService.create(createDto, "test@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Тестовый квиз");
        verify(quizDao).save(any(Quiz.class));
    }

    @Test
    void publishQuiz_withoutQuestions_shouldThrowException() {
        when(quizDao.findById(1L)).thenReturn(Optional.of(testQuiz));

        assertThatThrownBy(() -> quizService.publish(1L, "test@test.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Нельзя опубликовать квиз без вопросов");
    }

    @Test
    void deleteQuiz_byNonOwner_shouldThrowAccessDenied() {
        when(quizDao.findById(1L)).thenReturn(Optional.of(testQuiz));

        assertThatThrownBy(() -> quizService.delete(1L, "other@test.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void findQuiz_withNonExistingId_shouldThrowException() {
        when(quizDao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.findById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Квиз не найден");
    }
}
