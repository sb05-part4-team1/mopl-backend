package com.mopl.sse.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SseFacade {

    private final SseEmitterManager sseEmitterManager;

    public SseEmitter subscribe(UUID userId, UUID lastEventId) {
        SseEmitter emitter = sseEmitterManager.createEmitter(userId);

        try {
            emitter.send(SseEmitter.event()
                .id(sseEmitterManager.generateEventId().toString())
                .name("connect")
                .data("Connected"));
        } catch (IOException e) {
            log.error("Failed to send connect event to user: {}", userId, e);
            emitter.completeWithError(e);
            return emitter;
        }

        if (lastEventId != null) {
            sseEmitterManager.resendEventsAfter(userId, lastEventId, emitter);
        }

        return emitter;
    }
}
