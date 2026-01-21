package com.mopl.jpa.repository.outbox;

import com.mopl.domain.model.outbox.OutboxModel;
import com.mopl.domain.repository.outbox.OutboxRepository;
import com.mopl.jpa.entity.outbox.OutboxEntity;
import com.mopl.jpa.entity.outbox.OutboxEntityMapper;
import com.mopl.jpa.entity.outbox.OutboxEventStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {

    private final JpaOutboxEventRepository jpaOutboxEventRepository;
    private final OutboxEntityMapper outboxEntityMapper;

    @Override
    public OutboxModel save(OutboxModel outboxModel) {
        OutboxEntity entity = outboxEntityMapper.toEntity(outboxModel);
        OutboxEntity savedEntity = jpaOutboxEventRepository.save(entity);
        return outboxEntityMapper.toModel(savedEntity);
    }

    @Override
    public List<OutboxModel> findPendingEvents(int maxRetry, int limit) {
        return jpaOutboxEventRepository
            .findPendingEvents(OutboxEventStatus.PENDING, maxRetry, limit)
            .stream()
            .map(outboxEntityMapper::toModel)
            .toList();
    }

    @Override
    public int deletePublishedEventsBefore(Instant before) {
        return jpaOutboxEventRepository.deletePublishedEventsBefore(
            OutboxEventStatus.PUBLISHED, before
        );
    }
}
