package com.Karyakina.Ustenko.social.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_invites")
@Getter
@Setter
@NoArgsConstructor
public class GameInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "inviter_user_id", nullable = false)
    private Long inviterUserId;

    @Column(name = "invitee_user_id", nullable = false)
    private Long inviteeUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "invite_type", nullable = false, length = 30)
    private GameInviteType inviteType;

    @Column(name = "room_code", length = 64)
    private String roomCode;

    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
