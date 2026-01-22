package com.mopl.redis.repository.user;

import java.time.Duration;
import java.util.Optional;

import com.mopl.domain.repository.user.TemporaryPasswordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisTemporaryPasswordRepository implements TemporaryPasswordRepository {

    public static final String KEY_PREFIX = "user:temp-password:";
    public static final Duration TTL = Duration.ofMinutes(3);

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Optional<String> findByEmail(String email) {
        String key = buildKey(email);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value.toString());
    }

    @Override
    public void save(String email, String encodedPassword) {
        String key = buildKey(email);
        redisTemplate.opsForValue().set(key, encodedPassword, TTL);
    }

    @Override
    public void deleteByEmail(String email) {
        String key = buildKey(email);
        redisTemplate.delete(key);
    }

    private String buildKey(String email) {
        return KEY_PREFIX + email;
    }
}
