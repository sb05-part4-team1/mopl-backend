package com.mopl.sse.application;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.repository.notification.NotificationQueryRepository;
import com.mopl.sse.repository.RedisEmitterRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseEmitterManager {

    private static final long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private static final TimeBasedEpochGenerator UUID_V7_GENERATOR = Generators.timeBasedEpochGenerator();

    private final RedisEmitterRepository emitterRepository;
    private final NotificationQueryRepository notificationQueryRepository;
    private final MeterRegistry meterRegistry;

    private Counter eventSentCounter;
    private Counter eventFailedCounter;
    private Counter resendCounter;

    @PostConstruct
    public void initMetrics() {
        Gauge.builder("sse.connections", emitterRepository.getLocalEmitters(), Map::size)
            .description("Current SSE connections")
            .register(meterRegistry);

        eventSentCounter = Counter.builder("sse.events.sent")
            .description("SSE events sent successfully")
            .register(meterRegistry);

        eventFailedCounter = Counter.builder("sse.events.failed")
            .description("SSE events failed to send")
            .register(meterRegistry);

        resendCounter = Counter.builder("sse.events.resent")
            .description("SSE events resent")
            .register(meterRegistry);
    }

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
                eventSentCounter.increment();
                log.debug("Sent {} event to user: {}", eventName, userId);
            } catch (IOException e) {
                eventFailedCounter.increment();
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

        if (!cachedEvents.isEmpty()) {
            resendFromCache(cachedEvents, emitter, userId);
            return;
        }

        Instant lastEventTime = extractInstantFromUuidV7(lastEventId);
        List<NotificationModel> notifications = notificationQueryRepository.findByReceiverIdAndCreatedAtAfter(userId, lastEventTime);

        for (NotificationModel notification : notifications) {
            try {
                UUID eventId = generateEventId();
                emitter.send(SseEmitter.event()
                    .id(eventId.toString())
                    .name("notifications")
                    .data(notification));
                resendCounter.increment();
                log.debug("Resent from DB event {} to user: {}", eventId, userId);
            } catch (IOException e) {
                eventFailedCounter.increment();
                log.error("Failed to resend from DB to user: {}", userId, e);
                break;
            }
        }
    }

    private void resendFromCache(
        List<RedisEmitterRepository.CachedEvent> cachedEvents,
        SseEmitter emitter,
        UUID userId
    ) {
        for (RedisEmitterRepository.CachedEvent cachedEvent : cachedEvents) {
            try {
                emitter.send(SseEmitter.event()
                    .id(cachedEvent.eventId().toString())
                    .name("notifications")
                    .data(cachedEvent.data()));
                resendCounter.increment();
                log.debug("Resent from cache event {} to user: {}", cachedEvent.eventId(), userId);
            } catch (IOException e) {
                eventFailedCounter.increment();
                log.error("Failed to resend from cache to user: {}", userId, e);
                break;
            }
        }
    }

    private Instant extractInstantFromUuidV7(UUID uuid) {
        long timestamp = (uuid.getMostSignificantBits() >> 16) & 0xFFFFFFFFFFFFL;
        return Instant.ofEpochMilli(timestamp);
    }

    @Scheduled(fixedRate = 30000)
    public void sendHeartbeat() {
        Map<UUID, SseEmitter> emitters = emitterRepository.getLocalEmitters();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            emitters.forEach((userId, emitter) -> executor.execute(() -> {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                } catch (IOException e) {
                    log.debug("Heartbeat failed for user: {}, removing emitter", userId);
                    emitterRepository.deleteByUserId(userId);
                }
            })
            );
        }
    }
}
