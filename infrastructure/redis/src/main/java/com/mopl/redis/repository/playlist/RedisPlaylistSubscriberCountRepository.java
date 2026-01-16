package com.mopl.redis.repository.playlist;

import com.mopl.domain.repository.playlist.PlaylistSubscriberCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisPlaylistSubscriberCountRepository implements PlaylistSubscriberCountRepository {

    private static final String KEY_PREFIX = "playlist:subscriber:count:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public long getCount(UUID playlistId) {
        String key = buildKey(playlistId);
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return 0L;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        return Long.parseLong(value.toString());
    }

    @Override
    public void increment(UUID playlistId) {
        String key = buildKey(playlistId);
        redisTemplate.opsForValue().increment(key);
    }

    @Override
    public void decrement(UUID playlistId) {
        String key = buildKey(playlistId);
        Long currentValue = redisTemplate.opsForValue().decrement(key);

        if (currentValue != null && currentValue < 0) {
            redisTemplate.opsForValue().set(key, 0L);
        }
    }

    @Override
    public void setCount(UUID playlistId, long count) {
        String key = buildKey(playlistId);
        redisTemplate.opsForValue().set(key, count);
    }

    private String buildKey(UUID playlistId) {
        return KEY_PREFIX + playlistId.toString();
    }
}
