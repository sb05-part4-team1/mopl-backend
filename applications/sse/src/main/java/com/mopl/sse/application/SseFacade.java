package com.mopl.sse.application;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseFacade {

    private final SseEmitterManager sseEmitterManager;

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = sseEmitterManager.createEmitter(userId);

        try {
            emitter.send(SseEmitter.event()
                .id(UUID.randomUUID().toString())
                .name("connect")
                .data("Connected"));
        } catch (IOException e) {
            log.error("Failed to send connect event to user: {}", userId, e);
        }

        return emitter;
    }

    public void sendNotification(UUID userId, Object data) {
        sseEmitterManager.sendToUser(userId, "notification", data);
    }

    public boolean hasConnection(UUID userId) {
        return sseEmitterManager.hasLocalEmitter(userId);
    }
}
