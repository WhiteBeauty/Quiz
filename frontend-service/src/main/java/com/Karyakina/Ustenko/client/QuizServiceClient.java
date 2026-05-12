package com.Karyakina.Ustenko.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.Karyakina.Ustenko.dto.*;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${quiz-service.url}")
    private String quizServiceUrl;

    public AuthResponseDto register(String email, String username, String password) {
        return webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/auth/register")
                .bodyValue(Map.of("email", email, "username", username, "password", password))
                .retrieve()
                .bodyToMono(AuthResponseDto.class)
                .block();
    }

    public AuthResponseDto login(String email, String password) {
        return webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/auth/login")
                .bodyValue(Map.of("email", email, "password", password))
                .retrieve()
                .bodyToMono(AuthResponseDto.class)
                .block();
    }

    public UserDto getCurrentUser(String token) {
        return webClientBuilder.build()
                .get()
                .uri(quizServiceUrl + "/api/auth/me")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
    }

    public List<QuizDto> getPublishedQuizzes() {
        return webClientBuilder.build()
                .get()
                .uri(quizServiceUrl + "/api/quizzes/published")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<QuizDto>>() {})
                .block();
    }

    public List<QuizDto> getMyQuizzes(String token) {
        return webClientBuilder.build()
                .get()
                .uri(quizServiceUrl + "/api/quizzes/my")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<QuizDto>>() {})
                .block();
    }

    public QuizDto getQuiz(Long id, String token) {
        return webClientBuilder.build()
                .get()
                .uri(quizServiceUrl + "/api/quizzes/" + id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(QuizDto.class)
                .block();
    }

    public QuizDto createQuiz(String title, String description, String category, String token) {
        return webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/quizzes")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of("title", title, "description", description, "category", category))
                .retrieve()
                .bodyToMono(QuizDto.class)
                .block();
    }

    public QuizDto publishQuiz(Long quizId, String token) {
        return webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/quizzes/" + quizId + "/publish")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(QuizDto.class)
                .block();
    }

    public void deleteQuiz(Long quizId, String token) {
        webClientBuilder.build()
                .delete()
                .uri(quizServiceUrl + "/api/quizzes/" + quizId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void addQuestion(Long quizId, String text, int timeLimitSeconds, int points,
                            List<Map<String, Object>> answerOptions, String token) {
        webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/quizzes/" + quizId + "/questions")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of(
                        "text", text,
                        "timeLimitSeconds", timeLimitSeconds,
                        "points", points,
                        "answerOptions", answerOptions
                ))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void updateQuestion(Long questionId, String text, int timeLimitSeconds, int points,
                               List<Map<String, Object>> answerOptions, String token) {
        webClientBuilder.build()
                .put()
                .uri(quizServiceUrl + "/api/questions/" + questionId)
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of(
                        "text", text,
                        "timeLimitSeconds", timeLimitSeconds,
                        "points", points,
                        "answerOptions", answerOptions
                ))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void deleteQuestion(Long questionId, String token) {
        webClientBuilder.build()
                .delete()
                .uri(quizServiceUrl + "/api/questions/" + questionId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public List<QuizDto> searchQuizzes(String keyword) {
        return webClientBuilder.build()
                .get()
                .uri(quizServiceUrl + "/api/quizzes/search?keyword=" + keyword)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<QuizDto>>() {})
                .block();
    }
}
