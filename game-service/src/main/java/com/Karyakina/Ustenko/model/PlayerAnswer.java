package com.Karyakina.Ustenko.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_answers")
@Getter
@Setter
@NoArgsConstructor
public class PlayerAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gameRoomId;

    @Column(nullable = false)
    private Long participantId;

    @Column(nullable = false)
    private Long questionId;

    private Long selectedOptionId;

    private boolean correct;

    private int pointsEarned;

    @Column(nullable = false)
    private LocalDateTime answeredAt = LocalDateTime.now();

    private long responseTimeMs;
}
