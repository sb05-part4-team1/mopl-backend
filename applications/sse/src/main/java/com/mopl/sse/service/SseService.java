package com.mopl.sse.service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mopl.sse.repository.EmitterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SseService {

    // SSE 연결 지속 시간 설정(1시간)
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

        // 유실 방지를 위한 캐시 저장
        emitterRepository.saveEventCache(eventId, data);

        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .id(eventId)
                    .name(eventName) // "notification", "direct-messages"으로 구분
                    .data(data));
            } catch (IOException e) {
                emitterRepository.deleteById(id);
            }
        });
    }

    public void sendLostData(UUID lastEventId, UUID userId, SseEmitter emitter) {
        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByUserId(
            userId);
        String lastEventIdStr = lastEventId.toString();

        eventCaches.entrySet().stream()
            .filter(entry -> lastEventIdStr.compareTo(entry.getKey()) < 0)
            .forEach(entry -> send(emitter, entry.getKey(), "sse", entry.getValue()));
    }

    public String makeTimeIncludeId(UUID userId) {
        return userId.toString() + "_" + System.currentTimeMillis();
    }
}
