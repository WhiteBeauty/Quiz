package com.Karyakina.Ustenko.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.Karyakina.Ustenko.dto.*;
import com.Karyakina.Ustenko.dto.social.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    public List<UserSearchHitDto> searchFriends(String q, String token) {
        return webClientBuilder.build()
                .get()
                .uri(quizServiceUrl + "/api/friends/search?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8))
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UserSearchHitDto>>() {})
                .block();
    }

    public void sendFriendRequest(Long targetUserId, String token) {
        webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/friends/requests")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of("targetUserId", targetUserId))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void acceptFriendRequest(Long friendshipId, String token) {
        webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/friends/requests/" + friendshipId + "/accept")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void declineFriendRequest(Long friendshipId, String token) {
        webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/friends/requests/" + friendshipId + "/decline")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void cancelFriendRequest(Long friendshipId, String token) {
        webClientBuilder.build()
                .delete()
                .uri(quizServiceUrl + "/api/friends/requests/" + friendshipId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public List<FriendDto> listFriends(String token) {
        return webClientBuilder.build()
                .get()
                .uri(quizServiceUrl + "/api/friends")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FriendDto>>() {})
                .block();
    }

    public List<FriendRequestDto> listIncomingFriendRequests(String token) {
        return webClientBuilder.build()
                .get()
                .uri(quizServiceUrl + "/api/friends/requests/incoming")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FriendRequestDto>>() {})
                .block();
    }

    public List<FriendRequestDto> listOutgoingFriendRequests(String token) {
        return webClientBuilder.build()
                .get()
                .uri(quizServiceUrl + "/api/friends/requests/outgoing")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FriendRequestDto>>() {})
                .block();
    }

    public void removeFriend(Long friendUserId, String token) {
        webClientBuilder.build()
                .delete()
                .uri(quizServiceUrl + "/api/friends/" + friendUserId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public GameInviteSendResponseDto createGameInvite(String token, Map<String, Object> body) {
        return webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/game-invites")
                .header("Authorization", "Bearer " + token)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GameInviteSendResponseDto.class)
                .block();
    }

    public GameInviteResultDto consumeGameInvite(String rawToken, String token) {
        return webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/game-invites/consume")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of("token", rawToken))
                .retrieve()
                .bodyToMono(GameInviteResultDto.class)
                .block();
    }

    public ChatHistoryPageDto getChatMessages(Long peerUserId, String before, int limit, String token) {
        StringBuilder uri = new StringBuilder(quizServiceUrl + "/api/chat/" + peerUserId + "/messages?limit=" + limit);
        if (before != null && !before.isBlank()) {
            uri.append("&before=").append(URLEncoder.encode(before, StandardCharsets.UTF_8));
        }
        return webClientBuilder.build()
                .get()
                .uri(uri.toString())
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(ChatHistoryPageDto.class)
                .block();
    }

    public void sendChatMessage(Long peerUserId, String text, String token) {
        webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/chat/" + peerUserId + "/messages")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of("text", text))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void markChatRead(Long peerUserId, Long upToMessageId, String token) {
        webClientBuilder.build()
                .post()
                .uri(quizServiceUrl + "/api/chat/" + peerUserId + "/read?upToMessageId=" + upToMessageId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}