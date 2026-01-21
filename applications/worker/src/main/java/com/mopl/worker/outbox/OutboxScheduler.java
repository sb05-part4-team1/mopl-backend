package com.mopl.worker.outbox;

import com.mopl.domain.model.outbox.OutboxModel;
import com.mopl.domain.repository.outbox.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
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

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 1000)
    @SchedulerLock(name = "outbox_publish", lockAtLeastFor = "PT1S", lockAtMostFor = "PT5M")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxModel> pendingEvents = outboxRepository.findPendingEvents(MAX_RETRY, BATCH_SIZE);

        for (OutboxModel event : pendingEvents) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event: {}", event.getId(), ex);
                        }
                    });

                event.markAsPublished();
                outboxRepository.save(event);
                log.debug("Published event: {} to topic: {}", event.getId(), event.getTopic());

            } catch (Exception e) {
                log.error("Error publishing event: {}", event.getId(), e);
                event.incrementRetryCount();

                if (event.getRetryCount() >= MAX_RETRY) {
                    event.markAsFailed();
                    log.error("Event marked as FAILED after {} retries: {}", MAX_RETRY, event.getId());
                }
                outboxRepository.save(event);
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    @SchedulerLock(name = "outbox_cleanup", lockAtLeastFor = "PT1M", lockAtMostFor = "PT30M")
    @Transactional
    public void cleanupOldEvents() {
        Instant cutoff = Instant.now().minus(RETENTION_DAYS, ChronoUnit.DAYS);
        int deleted = outboxRepository.deletePublishedEventsBefore(cutoff);
        log.info("Cleaned up {} old outbox events", deleted);
    }
}
