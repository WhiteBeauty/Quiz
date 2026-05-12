package com.Karyakina.Ustenko.social.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Karyakina.Ustenko.dao.UserDao;
import com.Karyakina.Ustenko.model.User;
import com.Karyakina.Ustenko.social.dao.GameInviteDao;
import com.Karyakina.Ustenko.social.dto.GameInviteConsumeDto;
import com.Karyakina.Ustenko.social.dto.GameInviteCreateDto;
import com.Karyakina.Ustenko.social.dto.GameInviteResultDto;
import com.Karyakina.Ustenko.social.dto.GameInviteSendResponseDto;
import com.Karyakina.Ustenko.social.model.GameInvite;
import com.Karyakina.Ustenko.social.model.GameInviteType;
import com.Karyakina.Ustenko.social.redis.RedisRateLimiter;
import com.Karyakina.Ustenko.social.service.FriendshipService;
import com.Karyakina.Ustenko.social.service.GameInviteService;
import com.Karyakina.Ustenko.social.util.InviteTokens;
import com.Karyakina.Ustenko.util.SecurityUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameInviteServiceImpl implements GameInviteService {

    private final GameInviteDao gameInviteDao;
    private final FriendshipService friendshipService;
    private final UserDao userDao;
    private final RedisRateLimiter redisRateLimiter;
    private final ObjectProvider<JavaMailSender> javaMailSender;

    @Value("${app.frontend-base-url:http://localhost:8082}")
    private String frontendBaseUrl;

    @Value("${app.invite-token-ttl-hours:72}")
    private long inviteTtlHours;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Override
    @Transactional
    public GameInviteSendResponseDto createAndEmailInvite(GameInviteCreateDto dto, Authentication authentication) {
        Long inviterId = SecurityUtils.requireUserId(authentication);
        User inviter = userDao.findById(inviterId).orElseThrow();
        User invitee = userDao.findById(dto.getFriendUserId())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        if (!friendshipService.areFriends(inviterId, invitee.getId())) {
            throw new IllegalArgumentException("Приглашать в игру можно только друзей");
        }
        redisRateLimiter.check(
                "rl:invite:create:" + inviterId,
                20,
                Duration.ofMinutes(1),
                "Слишком много приглашений. Подождите."
        );
        if (dto.getInviteType() == GameInviteType.JOIN_EXISTING) {
            if (dto.getRoomCode() == null || dto.getRoomCode().isBlank()) {
                throw new IllegalArgumentException("Укажите код комнаты");
            }
        } else if (dto.getInviteType() == GameInviteType.CREATE_NEW_GAME) {
            if (dto.getQuizId() == null) {
                throw new IllegalArgumentException("Укажите квиз для новой игры");
            }
        } else {
            throw new IllegalArgumentException("Неизвестный тип приглашения");
        }

        String raw = InviteTokens.newRawToken();
        String hash = InviteTokens.sha256Hex(raw);

        GameInvite entity = new GameInvite();
        entity.setTokenHash(hash);
        entity.setInviterUserId(inviterId);
        entity.setInviteeUserId(invitee.getId());
        entity.setInviteType(dto.getInviteType());
        entity.setRoomCode(dto.getInviteType() == GameInviteType.JOIN_EXISTING ? dto.getRoomCode().trim() : null);
        entity.setQuizId(dto.getInviteType() == GameInviteType.CREATE_NEW_GAME ? dto.getQuizId() : null);
        entity.setExpiresAt(LocalDateTime.now().plusHours(inviteTtlHours));
        gameInviteDao.save(entity);

        String link = frontendBaseUrl.replaceAll("/$", "") + "/invite/game?t=" + raw;
        String subject = "Приглашение в игру Quiz от " + inviter.getUsername();
        String body = buildEmailBody(inviter.getUsername(), dto.getInviteType(), link);
        sendMail(invitee.getEmail(), subject, body);

        GameInviteSendResponseDto res = new GameInviteSendResponseDto();
        res.setInviteLink(link);
        res.setMessage(mailEnabled ? "Письмо отправлено" : "Почта отключена (app.mail.enabled=false), ссылка доступна в ответе");
        return res;
    }

    private void sendMail(String to, String subject, String text) {
        if (mailEnabled) {
            JavaMailSender sender = javaMailSender.getIfAvailable();
            if (sender != null) {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(to);
                msg.setSubject(subject);
                msg.setText(text);
                sender.send(msg);
                return;
            }
        }
        log.info("Game invite email (mail disabled or no JavaMailSender). To={} Subject={}\n{}", to, subject, text);
    }

    private static String buildEmailBody(String inviterName, GameInviteType type, String link) {
        String action = type == GameInviteType.JOIN_EXISTING
                ? "присоединиться к уже начатой игре"
                : "создать новую игру и пригласить вас";
        return "Здравствуйте!\n\n"
                + inviterName + " приглашает вас " + action + " на платформе Quiz.\n\n"
                + "Перейдите по одноразовой ссылке (действует ограниченное время):\n"
                + link + "\n\n"
                + "Если вы не ожидали это письмо, просто проигнорируйте его.\n";
    }

    @Override
    @Transactional
    public GameInviteResultDto consumeInvite(GameInviteConsumeDto dto, Authentication authentication) {
        Long me = SecurityUtils.requireUserId(authentication);
        String hash = InviteTokens.sha256Hex(dto.getToken().trim());
        GameInvite inv = gameInviteDao.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Недействительная или устаревшая ссылка"));
        if (inv.getUsedAt() != null) {
            throw new IllegalStateException("Ссылка уже использована");
        }
        if (inv.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Срок действия ссылки истёк");
        }
        if (!inv.getInviteeUserId().equals(me)) {
            throw new IllegalArgumentException("Это приглашение выдано другому пользователю");
        }
        inv.setUsedAt(LocalDateTime.now());
        gameInviteDao.save(inv);

        GameInviteResultDto out = new GameInviteResultDto();
        out.setInviteType(inv.getInviteType());
        if (inv.getInviteType() == GameInviteType.JOIN_EXISTING) {
            out.setAction("JOIN_ROOM");
            out.setRoomCode(inv.getRoomCode());
        } else {
            out.setAction("CREATE_ROOM");
            out.setQuizId(inv.getQuizId());
        }
        return out;
    }
}
