package com.Karyakina.Ustenko.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_results")
@Getter
@Setter
@NoArgsConstructor
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gameRoomId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private int finalScore;

    @Column(nullable = false)
    private int rank;

    private int correctAnswers;

    private int totalQuestions;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
