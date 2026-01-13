package com.mopl.sse.repository;

import java.util.Map;
import java.util.UUID;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface EmitterRepository {

    SseEmitter save(String emitterId, SseEmitter sseEmitter);

    void saveEventCache(String eventCacheId, Object event);

    Map<String, SseEmitter> findAllEmitterStartWithByUserId(UUID userId);

    Map<String, Object> findAllEventCacheStartWithByUserId(UUID userId);

    void deleteById(String id);

    void deleteAllEmitterStartWithId(UUID userId);

    void deleteAllEventCacheStartWithId(UUID userId);
}
