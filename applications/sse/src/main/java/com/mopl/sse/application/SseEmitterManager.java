package com.mopl.sse.application;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import com.mopl.sse.repository.RedisEmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseEmitterManager {

    private static final long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private static final TimeBasedEpochGenerator UUID_V7_GENERATOR = Generators.timeBasedEpochGenerator();

    private final RedisEmitterRepository emitterRepository;

    public SseEmitter createEmitter(UUID userId) {
        emitterRepository.findByUserId(userId).ifPresent(existing -> {
            log.debug("Closing existing emitter for user: {}", userId);
            existing.complete();
        });

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
        UUID eventId = generateEventId();

        emitterRepository.cacheEvent(userId, eventId, data);

        emitterRepository.findByUserId(userId).ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                    .id(eventId.toString())
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

    public UUID generateEventId() {
        return UUID_V7_GENERATOR.generate();
    }

    public void resendEventsAfter(UUID userId, UUID lastEventId, SseEmitter emitter) {
        List<RedisEmitterRepository.CachedEvent> cachedEvents = emitterRepository.getEventsAfter(userId, lastEventId);

        for (RedisEmitterRepository.CachedEvent cachedEvent : cachedEvents) {
            try {
                emitter.send(SseEmitter.event()
                    .id(cachedEvent.eventId().toString())
                    .name("notifications")
                    .data(cachedEvent.data()));
                log.debug("Resent event {} to user: {}", cachedEvent.eventId(), userId);
            } catch (IOException e) {
                log.error("Failed to resend event to user: {}", userId, e);
                break;
            }
        }
    }
}
