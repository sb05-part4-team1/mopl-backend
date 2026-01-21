package com.mopl.domain.repository.outbox;

import com.mopl.domain.model.outbox.OutboxModel;

import java.time.Instant;
import java.util.List;

public interface OutboxRepository {

    List<OutboxModel> findPendingEvents(int maxRetry, int limit);

    OutboxModel save(OutboxModel outboxModel);

    int deletePublishedEventsBefore(Instant before);
}
