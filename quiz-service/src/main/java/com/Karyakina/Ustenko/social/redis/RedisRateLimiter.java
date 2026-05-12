package com.Karyakina.Ustenko.social.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.Karyakina.Ustenko.social.RateLimitExceededException;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final StringRedisTemplate redisTemplate;

    public void check(String bucketKey, int maxEvents, Duration window, String errorMessage) {
        try {
            Long v = redisTemplate.opsForValue().increment(bucketKey);
            if (v != null && v == 1L) {
                redisTemplate.expire(bucketKey, window);
            }
            if (v != null && v > maxEvents) {
                throw new RateLimitExceededException(errorMessage);
            }
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception ex) {
            // fail-open: без Redis сервис остаётся доступен
        }
    }
}
