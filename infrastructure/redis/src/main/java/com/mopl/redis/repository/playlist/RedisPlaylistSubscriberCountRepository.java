package com.mopl.redis.repository.playlist;

import com.mopl.domain.repository.playlist.PlaylistSubscriberCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        return parseCount(value);
    }

    @Override
    public Map<UUID, Long> getCounts(Collection<UUID> playlistIds) {
        if (playlistIds.isEmpty()) {
            return Map.of();
        }

        List<UUID> playlistIdList = List.copyOf(playlistIds);
        List<String> keys = playlistIdList.stream()
            .map(this::buildKey)
            .toList();

        List<Object> values = redisTemplate.opsForValue().multiGet(keys);

        if (values == null) {
            return Map.of();
        }

        Map<UUID, Long> result = new HashMap<>();
        for (int i = 0; i < playlistIdList.size(); i++) {
            UUID playlistId = playlistIdList.get(i);
            Object value = values.get(i);
            long count = parseCount(value);
            result.put(playlistId, count);
        }

        return result;
    }

    @Override
    public void setCount(UUID playlistId, long count) {
        String key = buildKey(playlistId);
        redisTemplate.opsForValue().set(key, count);
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

    private long parseCount(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    private String buildKey(UUID playlistId) {
        return KEY_PREFIX + playlistId.toString();
    }
}
