package com.Karyakina.Ustenko.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.Karyakina.Ustenko.dto.QuestionCreateDto;
import com.Karyakina.Ustenko.dto.QuestionDto;
import com.Karyakina.Ustenko.service.QuestionService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<QuestionDto> addQuestion(
            @PathVariable Long quizId,
            @Valid @RequestBody QuestionCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.addQuestion(quizId, dto, userDetails.getUsername()));
    }

    @GetMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<List<QuestionDto>> findByQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(questionService.findByQuiz(quizId));
    }

    @PutMapping("/questions/{questionId}")
    public ResponseEntity<QuestionDto> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(questionService.updateQuestion(questionId, dto, userDetails.getUsername()));
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long questionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        questionService.deleteQuestion(questionId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
