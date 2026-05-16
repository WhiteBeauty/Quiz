package com.Karyakina.Ustenko.social.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Karyakina.Ustenko.dao.UserDao;
import com.Karyakina.Ustenko.model.User;
import com.Karyakina.Ustenko.social.dao.FriendshipDao;
import com.Karyakina.Ustenko.social.dto.FriendDto;
import com.Karyakina.Ustenko.social.dto.FriendRequestDto;
import com.Karyakina.Ustenko.social.dto.UserSearchHitDto;
import com.Karyakina.Ustenko.social.model.Friendship;
import com.Karyakina.Ustenko.social.model.FriendshipStatus;
import com.Karyakina.Ustenko.social.redis.FriendListRedisCache;
import com.Karyakina.Ustenko.social.redis.RedisRateLimiter;
import com.Karyakina.Ustenko.social.redis.UserSearchRedisCache;
import com.Karyakina.Ustenko.social.service.FriendshipService;
import com.Karyakina.Ustenko.util.SecurityUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private static final int SEARCH_PAGE_SIZE = 20;
    private static final Logger log = LoggerFactory.getLogger(FriendshipServiceImpl.class);

    private final FriendshipDao friendshipDao;
    private final UserDao userDao;
    private final RedisRateLimiter redisRateLimiter;
    private final FriendListRedisCache friendListRedisCache;
    private final UserSearchRedisCache userSearchRedisCache;

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchHitDto> searchUsers(String query, Authentication authentication) {
        Long me = SecurityUtils.requireUserId(authentication);
        redisRateLimiter.check(
                "rl:friend:search:" + me,
                40,
                Duration.ofMinutes(1),
                "Слишком много поисковых запросов. Попробуйте через минуту."
        );
        String q = query == null ? "" : query.trim();
        if (q.length() < 2) {
            throw new IllegalArgumentException("Введите не менее 2 символов");
        }

        log.info("Поиск пользователей: query={}, userId={}", q, me);

        // Пропускаем кэш, если Redis недоступен — ищем напрямую в БД
        List<User> found = userDao.searchByNicknameOrEmail(me, q, PageRequest.of(0, SEARCH_PAGE_SIZE));
        log.info("Найдено пользователей: {}", found.size());

        List<UserSearchHitDto> out = new ArrayList<>();
        for (User u : found) {
            UserSearchHitDto dto = new UserSearchHitDto();
            dto.setId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setEmail(u.getEmail());
            out.add(dto);
            log.debug("Найден пользователь: id={}, username={}, email={}", u.getId(), u.getUsername(), u.getEmail());
        }

        // Пытаемся кэшировать, но не если ошибка
        try {
            userSearchRedisCache.put(me, q, out);
        } catch (Exception e) {
            // Кэш недоступен — продолжаем без него
            log.warn("Redis cache unavailable for search: {}", e.getMessage());
        }
        return out;
    }

    @Override
    @Transactional
    public void sendFriendRequest(Long targetUserId, Authentication authentication) {
        Long from = SecurityUtils.requireUserId(authentication);
        if (from.equals(targetUserId)) {
            throw new IllegalArgumentException("Нельзя отправить заявку самому себе");
        }
        redisRateLimiter.check(
                "rl:friend:req:" + from,
                30,
                Duration.ofMinutes(1),
                "Слишком много заявок в друзья. Подождите немного."
        );
        userDao.findById(targetUserId).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        long u1 = Math.min(from, targetUserId);
        long u2 = Math.max(from, targetUserId);

        Optional<Friendship> existing = friendshipDao.findByUser1IdAndUser2Id(u1, u2);
        if (existing.isEmpty()) {
            Friendship f = new Friendship();
            f.setUser1Id(u1);
            f.setUser2Id(u2);
            f.setInitiatorId(from);
            f.setStatus(FriendshipStatus.PENDING);
            friendshipDao.save(f);
            return;
        }
        Friendship f = existing.get();
        if (f.getStatus() == FriendshipStatus.ACCEPTED) {
            throw new IllegalStateException("Вы уже друзья");
        }
        if (f.getInitiatorId().equals(from)) {
            throw new IllegalStateException("Заявка уже отправлена");
        }
        f.setStatus(FriendshipStatus.ACCEPTED);
        friendshipDao.save(f);
        friendListRedisCache.evictTwo(from, targetUserId);
    }

    @Override
    @Transactional
    public void acceptFriendRequest(Long friendshipId, Authentication authentication) {
        Long me = SecurityUtils.requireUserId(authentication);
        Friendship f = friendshipDao.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        if (f.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Заявка уже обработана");
        }
        if (f.getInitiatorId().equals(me)) {
            throw new IllegalArgumentException("Нельзя принять собственную заявку");
        }
        if (!f.getUser1Id().equals(me) && !f.getUser2Id().equals(me)) {
            throw new IllegalArgumentException("Заявка не адресована вам");
        }
        f.setStatus(FriendshipStatus.ACCEPTED);
        friendshipDao.save(f);
        long other = f.getUser1Id().equals(me) ? f.getUser2Id() : f.getUser1Id();
        friendListRedisCache.evictTwo(me, other);
    }

    @Override
    @Transactional
    public void declineFriendRequest(Long friendshipId, Authentication authentication) {
        Long me = SecurityUtils.requireUserId(authentication);
        Friendship f = friendshipDao.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        if (f.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Заявка уже обработана");
        }
        if (!f.getUser1Id().equals(me) && !f.getUser2Id().equals(me)) {
            throw new IllegalArgumentException("Заявка не адресована вам");
        }
        long other = f.getUser1Id().equals(me) ? f.getUser2Id() : f.getUser1Id();
        friendshipDao.delete(f);
        friendListRedisCache.evictTwo(me, other);
    }

    @Override
    @Transactional
    public void cancelOutgoingRequest(Long friendshipId, Authentication authentication) {
        Long me = SecurityUtils.requireUserId(authentication);
        Friendship f = friendshipDao.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        if (f.getStatus() != FriendshipStatus.PENDING || !f.getInitiatorId().equals(me)) {
            throw new IllegalArgumentException("Нет исходящей заявки с таким идентификатором");
        }
        long other = f.getUser1Id().equals(me) ? f.getUser2Id() : f.getUser1Id();
        friendshipDao.delete(f);
        friendListRedisCache.evictTwo(me, other);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendDto> listFriends(Authentication authentication) {
        Long me = SecurityUtils.requireUserId(authentication);
        List<FriendDto> cached = friendListRedisCache.get(me);
        if (cached != null) {
            return cached;
        }
        List<Friendship> rows = friendshipDao.findAcceptedForUser(me);
        List<FriendDto> list = new ArrayList<>();
        for (Friendship f : rows) {
            long otherId = f.getUser1Id().equals(me) ? f.getUser2Id() : f.getUser1Id();
            User u = userDao.findById(otherId).orElse(null);
            if (u == null) {
                continue;
            }
            FriendDto dto = new FriendDto();
            dto.setUserId(u.getId());
            dto.setUsername(u.getUsername());
            list.add(dto);
        }
        friendListRedisCache.put(me, list);
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendRequestDto> listIncomingRequests(Authentication authentication) {
        Long me = SecurityUtils.requireUserId(authentication);
        List<Friendship> rows = friendshipDao.findPendingIncoming(me);
        return toRequestDtos(me, rows, "incoming");
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendRequestDto> listOutgoingRequests(Authentication authentication) {
        Long me = SecurityUtils.requireUserId(authentication);
        List<Friendship> rows = friendshipDao.findPendingOutgoing(me);
        return toRequestDtos(me, rows, "outgoing");
    }

    private List<FriendRequestDto> toRequestDtos(Long me, List<Friendship> rows, String direction) {
        List<FriendRequestDto> out = new ArrayList<>();
        for (Friendship f : rows) {
            long otherId = f.getUser1Id().equals(me) ? f.getUser2Id() : f.getUser1Id();
            User u = userDao.findById(otherId).orElse(null);
            if (u == null) {
                continue;
            }
            FriendRequestDto dto = new FriendRequestDto();
            dto.setFriendshipId(f.getId());
            dto.setOtherUserId(u.getId());
            dto.setOtherUsername(u.getUsername());
            dto.setDirection(direction);
            out.add(dto);
        }
        return out;
    }

    @Override
    @Transactional
    public void removeFriend(Long friendUserId, Authentication authentication) {
        Long me = SecurityUtils.requireUserId(authentication);
        long u1 = Math.min(me, friendUserId);
        long u2 = Math.max(me, friendUserId);
        Friendship f = friendshipDao.findByUser1IdAndUser2Id(u1, u2)
                .orElseThrow(() -> new IllegalArgumentException("Дружба не найдена"));
        if (f.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new IllegalStateException("Пользователь не в списке друзей");
        }
        friendshipDao.delete(f);
        friendListRedisCache.evictTwo(me, friendUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean areFriends(Long userIdA, Long userIdB) {
        if (userIdA == null || userIdB == null || userIdA.equals(userIdB)) {
            return false;
        }
        long u1 = Math.min(userIdA, userIdB);
        long u2 = Math.max(userIdA, userIdB);
        return friendshipDao.findByUser1IdAndUser2Id(u1, u2)
                .filter(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                .isPresent();
    }
}
