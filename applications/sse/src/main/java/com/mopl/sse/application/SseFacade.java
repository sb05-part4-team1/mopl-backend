package com.mopl.sse.application;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.sse.interfaces.api.NotificationResponse;
import com.mopl.sse.interfaces.api.NotificationResponseMapper;
import com.mopl.sse.service.SseService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SseFacade {

    private final SseService sseService;
    private final NotificationResponseMapper notificationMapper;

    public SseEmitter subscribe(UUID userId, UUID lastEventId) {
        // Emitter 생성 및 저장
        SseEmitter emitter = sseService.createEmitter(userId);

        // 연결 직후 더미 데이터 전송
        String eventId = sseService.makeTimeIncludeId(userId);
        sseService.send(emitter, eventId, "sse", "EventStream Created.");

        // 미수신 데이터 재전송
        if (lastEventId != null) {
            sseService.sendLostData(lastEventId, userId, emitter);
        }

        return emitter;
    }

    public void sendNotification(NotificationModel model) {
        NotificationResponse response = notificationMapper.toResponse(model);
        sseService.sendToUser(model.getReceiverId(), "notification", response);
    }

    // TODO: DM 알림 메서드 추가 구현 필요
}
