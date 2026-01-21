package com.mopl.sse.application;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mopl.sse.repository.RedisEmitterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseEmitterManager {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private final RedisEmitterRepository emitterRepository;

    public SseEmitter createEmitter(UUID userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(userId, emitter);

        emitter.onCompletion(() -> {
            log.debug("Emitter completed for user: {}", userId);
            emitterRepository.deleteByUserId(userId);
        });
        emitter.onTimeout(() -> {
            log.debug("Emitter timed out for user: {}", userId);
            emitterRepository.deleteByUserId(userId);
        });
        emitter.onError((e) -> {
            log.debug("Emitter error for user: {}", userId, e);
            emitterRepository.deleteByUserId(userId);
        });

        return emitter;
    }

    public void sendToUser(UUID userId, String eventName, Object data) {
        emitterRepository.findByUserId(userId).ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                    .id(UUID.randomUUID().toString())
                    .name(eventName)
                    .data(data));
                log.debug("Sent {} event to user: {}", eventName, userId);
            } catch (IOException e) {
                log.error("Failed to send event to user: {}", userId, e);
                emitterRepository.deleteByUserId(userId);
            }
        });
    }

    public boolean hasLocalEmitter(UUID userId) {
        return emitterRepository.existsLocally(userId);
    }
}
