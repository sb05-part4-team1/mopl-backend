package com.mopl.worker.outbox;

import com.mopl.jpa.entity.outbox.OutboxEventEntity;
import com.mopl.jpa.entity.outbox.OutboxEventStatus;
import com.mopl.jpa.repository.outbox.JpaOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY = 3;
    private static final int RETENTION_DAYS = 7;

    private final JpaOutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEventEntity> pendingEvents = outboxEventRepository.findPendingEvents(
            OutboxEventStatus.PENDING, MAX_RETRY, BATCH_SIZE
        );

        for (OutboxEventEntity event : pendingEvents) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event: {}", event.getId(), ex);
                        }
                    });

                event.markAsPublished();
                log.debug("Published event: {} to topic: {}", event.getId(), event.getTopic());

            } catch (Exception e) {
                log.error("Error publishing event: {}", event.getId(), e);
                event.incrementRetryCount();

                if (event.getRetryCount() >= MAX_RETRY) {
                    event.markAsFailed();
                    log.error("Event marked as FAILED after {} retries: {}", MAX_RETRY, event
                        .getId());
                }
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldEvents() {
        Instant cutoff = Instant.now().minus(RETENTION_DAYS, ChronoUnit.DAYS);
        int deleted = outboxEventRepository.deletePublishedEventsBefore(
            OutboxEventStatus.PUBLISHED, cutoff
        );
        log.info("Cleaned up {} old outbox events", deleted);
    }
}
