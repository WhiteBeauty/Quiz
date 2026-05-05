package com.Karyakina.Ustenko.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.Karyakina.Ustenko.dto.QuizCreateDto;
import com.Karyakina.Ustenko.dto.QuizDto;
import com.Karyakina.Ustenko.service.QuizService;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    public ResponseEntity<QuizDto> create(
            @Valid @RequestBody QuizCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quizService.create(dto, userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.findById(id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<QuizDto>> findMyQuizzes(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(quizService.findByAuthor(userDetails.getUsername()));
    }

    @GetMapping("/published")
    public ResponseEntity<List<QuizDto>> findAllPublished() {
        return ResponseEntity.ok(quizService.findAllPublished());
    }

    @GetMapping("/search")
    public ResponseEntity<List<QuizDto>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(quizService.search(keyword));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<QuizDto> publish(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(quizService.publish(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizDto> update(
            @PathVariable Long id,
            @Valid @RequestBody QuizCreateDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(quizService.update(id, dto, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        quizService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
