package com.mopl.domain.repository.outbox;

import com.mopl.domain.model.outbox.OutboxModel;

public interface OutboxRepository {

    OutboxModel save(OutboxModel outboxModel);
}
