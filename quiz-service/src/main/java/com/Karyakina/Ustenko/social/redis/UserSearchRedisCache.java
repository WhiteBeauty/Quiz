package com.Karyakina.Ustenko.social.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.Karyakina.Ustenko.social.dto.UserSearchHitDto;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserSearchRedisCache {

    private static final Duration TTL = Duration.ofMinutes(5);
    private static final String PREFIX = "friends:search:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static String key(Long userId, String q) {
        return PREFIX + userId + ":" + q.trim().toLowerCase().replaceAll("[^a-z0-9@.]", "_");
    }

    public List<UserSearchHitDto> get(Long userId, String q) {
        try {
            String json = redisTemplate.opsForValue().get(key(userId, q));
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    public void put(Long userId, String q, List<UserSearchHitDto> hits) {
        try {
            String json = objectMapper.writeValueAsString(hits == null ? Collections.emptyList() : hits);
            redisTemplate.opsForValue().set(key(userId, q), json, TTL);
        } catch (Exception ignored) {
        }
    }
}
