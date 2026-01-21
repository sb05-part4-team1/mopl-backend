package com.mopl.sse.application;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mopl.sse.repository.EmitterRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SseEmitterManager {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private final EmitterRepository emitterRepository;

    public SseEmitter createEmitter(UUID userId) {
        String emitterId = makeTimeIncludeId(userId);
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));
        emitter.onError((e) -> emitterRepository.deleteById(emitterId));

        return emitter;
    }

    public void send(SseEmitter emitter, String eventId, String name, Object data) {
        try {
            emitter.send(SseEmitter.event()
                .id(eventId)
                .name(name)
                .data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    public void sendToUser(UUID receiverId, String eventName, Object data) {
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByUserId(
            receiverId);
        String eventId = makeTimeIncludeId(receiverId);

        emitterRepository.saveEventCache(eventId, data);

        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .id(eventId)
                    .name(eventName)
                    .data(data));
            } catch (IOException e) {
                emitterRepository.deleteById(id);
            }
        });
    }

    public void sendLostData(String lastEventId, UUID userId, SseEmitter emitter) {
        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByUserId(
            userId);

        eventCaches.entrySet().stream()
            .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
            .forEach(entry -> send(emitter, entry.getKey(), "sse", entry.getValue()));
    }

    public String makeTimeIncludeId(UUID userId) {
        return userId.toString() + "_" + System.currentTimeMillis();
    }
}
