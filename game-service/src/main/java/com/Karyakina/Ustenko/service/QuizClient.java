package com.Karyakina.Ustenko.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.Karyakina.Ustenko.dto.QuizInfoDto;

@Service
@RequiredArgsConstructor
public class QuizClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${quiz-service.url}")
    private String quizServiceUrl;

    public QuizInfoDto getQuiz(Long quizId, String authToken) {
        return webClientBuilder.build()
                .get()
                .uri(quizServiceUrl + "/api/quizzes/" + quizId)
                .header("Authorization", "Bearer " + authToken)
                .retrieve()
                .bodyToMono(QuizInfoDto.class)
                .block();
    }
}
