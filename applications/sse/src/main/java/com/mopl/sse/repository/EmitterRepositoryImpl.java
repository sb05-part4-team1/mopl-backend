package com.mopl.sse.repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository {

    // 동시성을 고려한 ConcurrentHashMap 사용
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

    // emitter 저장
    @Override
    public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
        emitters.put(emitterId, sseEmitter);
        return sseEmitter;
    }

    // 이벤트를 저장
    @Override
    public void saveEventCache(String eventCacheId, Object event) {
        eventCache.put(eventCacheId, event);
    }

    // 해당 회원과 관련된 모든 이벤트를 찾음
    @Override
    public Map<String, SseEmitter> findAllEmitterStartWithByUserId(UUID userId) {
        String userIdStr = userId.toString();
        return emitters.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(userIdStr))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, Object> findAllEventCacheStartWithByUserId(UUID userId) {
        String userIdStr = userId.toString();
        return eventCache.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(userIdStr))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // emitter를 지움
    @Override
    public void deleteById(String id) {
        emitters.remove(id);
    }

    // 해당 회원과 관련된 모든 emitter를 지움
    @Override
    public void deleteAllEmitterStartWithId(UUID userId) {
        String userIdStr = userId.toString();
        emitters.forEach((key, emitter) -> {
            if (key.startsWith(userIdStr)) {
                emitters.remove(key);
            }
        });
    }

    // 해당 회원과 관련된 모든 이벤트를 지움
    @Override
    public void deleteAllEventCacheStartWithId(UUID userId) {
        String userIdStr = userId.toString();
        eventCache.forEach((key, data) -> {
            if (key.startsWith(userIdStr)) {
                eventCache.remove(key);
            }
        });
    }
}
