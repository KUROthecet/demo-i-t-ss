package com.aims.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisTokenBlacklist implements TokenBlacklist {

    private static final String KEY_PREFIX = "jwt:bl:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void add(String token, long ttlMs) {
        if (ttlMs > 0) {
            redisTemplate.opsForValue().set(KEY_PREFIX + token, "1", ttlMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public boolean contains(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + token));
    }
}
