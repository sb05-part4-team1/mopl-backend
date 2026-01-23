package com.mopl.domain.service.outbox;

import com.mopl.domain.model.outbox.OutboxModel;
import com.mopl.domain.repository.outbox.OutboxRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepository;

    public OutboxModel save(OutboxModel outboxModel) {
        return outboxRepository.save(outboxModel);
    }
}
