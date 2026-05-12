package com.Karyakina.Ustenko.social.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.Karyakina.Ustenko.social.dto.FriendDto;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FriendListRedisCache {

    private static final Duration TTL = Duration.ofSeconds(45);
    private static final String PREFIX = "friends:list:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public List<FriendDto> get(Long userId) {
        try {
            String json = redisTemplate.opsForValue().get(PREFIX + userId);
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    public void put(Long userId, List<FriendDto> friends) {
        try {
            String json = objectMapper.writeValueAsString(friends == null ? Collections.emptyList() : friends);
            redisTemplate.opsForValue().set(PREFIX + userId, json, TTL);
        } catch (Exception ignored) {
        }
    }

    public void evict(Long userId) {
        try {
            redisTemplate.delete(PREFIX + userId);
        } catch (Exception ignored) {
        }
    }

    public void evictTwo(Long userIdA, Long userIdB) {
        evict(userIdA);
        evict(userIdB);
    }
}
