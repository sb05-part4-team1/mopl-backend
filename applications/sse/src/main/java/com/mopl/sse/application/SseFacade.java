package com.mopl.sse.application;

import com.mopl.domain.model.notification.NotificationModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SseFacade {

    private final SseEmitterManager sseEmitterManager;

    public SseEmitter subscribe(UUID userId, String lastEventId) {
        SseEmitter emitter = sseEmitterManager.createEmitter(userId);

        String eventId = sseEmitterManager.makeTimeIncludeId(userId);
        sseEmitterManager.send(emitter, eventId, "sse", "EventStream Created.");

        if (lastEventId != null && !lastEventId.isEmpty()) {
            sseEmitterManager.sendLostData(lastEventId, userId, emitter);
        }

        return emitter;
    }

    public void sendNotification(NotificationModel model) {
        SseNotificationData data = SseNotificationData.from(model);
        sseEmitterManager.sendToUser(model.getReceiver().getId(), "notification", data);
    }

    public void sendToUser(UUID userId, String eventName, Object data) {
        sseEmitterManager.sendToUser(userId, eventName, data);
    }
}
