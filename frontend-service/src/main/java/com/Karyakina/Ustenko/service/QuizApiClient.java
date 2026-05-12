package com.Karyakina.Ustenko.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.Karyakina.Ustenko.dto.AuthDto;
import com.Karyakina.Ustenko.dto.QuizDto;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizApiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${quiz-service.url}")
    private String quizServiceUrl;

    public AuthDto register(String email, String username, String password) {
        return webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/auth/register")
                .bodyValue(Map.of("email", email, "username", username, "password", password))
                .retrieve()
                .bodyToMono(AuthDto.class)
                .block();
    }

    public AuthDto login(String email, String password) {
        return webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/auth/login")
                .bodyValue(Map.of("email", email, "password", password))
                .retrieve()
                .bodyToMono(AuthDto.class)
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
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<QuizDto>>() {})
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

    public QuizDto getQuiz(Long id, String token) {
        return webClientBuilder.build()
                .get()
                .uri(quizServiceUrl + "/api/quizzes/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(QuizDto.class)
                .block();
    }

    public QuizDto createQuiz(String title, String description, String category, String token) {
        return webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/quizzes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(Map.of("title", title, "description", description, "category", category))
                .retrieve()
                .bodyToMono(QuizDto.class)
                .block();
    }

    public QuizDto publishQuiz(Long id, String token) {
        return webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/quizzes/" + id + "/publish")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(QuizDto.class)
                .block();
    }

    public void addQuestion(Long quizId, Map<String, Object> questionData, String token) {
        webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/quizzes/" + quizId + "/questions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(questionData)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void deleteQuiz(Long id, String token) {
        webClientBuilder.build()
                .delete()
                .uri(quizServiceUrl + "/api/quizzes/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
