package com.mopl.worker.outbox;

import com.mopl.domain.model.outbox.OutboxModel;
import com.mopl.domain.repository.outbox.OutboxRepository;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    public void publishPendingEvents() {
        try (LogContext.Scope ignored = LogContext.scoped("job", "outboxPublish")) {
            List<OutboxModel> pendingEvents = outboxRepository.findPendingEvents(MAX_RETRY, BATCH_SIZE);

            for (OutboxModel event : pendingEvents) {
                try {
                    kafkaTemplate.send(
                        event.getTopic(),
                        event.getAggregateId(),
                        event.getPayload()
                    ).get(5, TimeUnit.SECONDS);
                    event.markAsPublished();
                    outboxRepository.save(event);
                    LogContext.with("eventId", event.getId())
                        .and("topic", event.getTopic())
                        .debug("Published event");
                } catch (Exception e) {
                    LogContext.with("eventId", event.getId()).error("Error publishing event", e);
                    event.incrementRetryCount();

                    if (event.getRetryCount() >= MAX_RETRY) {
                        event.markAsFailed();
                        LogContext.with("eventId", event.getId())
                            .and("maxRetry", MAX_RETRY)
                            .error("Event marked as FAILED after max retries");
                    }

                    try {
                        outboxRepository.save(event);
                    } catch (Exception saveEx) {
                        LogContext.with("eventId", event.getId()).error("Failed to save event status", saveEx);
                    }
                }
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    @SchedulerLock(name = "outbox_cleanup", lockAtLeastFor = "PT1M", lockAtMostFor = "PT30M")
    public void cleanupOldEvents() {
        Instant cutoff = Instant.now().minus(RETENTION_DAYS, ChronoUnit.DAYS);
        int deleted = outboxRepository.deletePublishedEventsBefore(cutoff);
        LogContext.with("job", "outboxCleanup")
            .and("deletedCount", deleted)
            .info("Cleaned up old outbox events");
    }
}
