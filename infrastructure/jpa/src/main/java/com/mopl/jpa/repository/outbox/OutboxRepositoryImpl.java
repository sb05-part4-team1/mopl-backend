package com.mopl.jpa.repository.outbox;

import com.mopl.domain.model.outbox.OutboxModel;
import com.mopl.domain.repository.outbox.OutboxRepository;
import com.mopl.jpa.entity.outbox.OutboxEntity;
import com.mopl.jpa.entity.outbox.OutboxEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {

    private final JpaOutboxRepository jpaOutboxRepository;
    private final OutboxEntityMapper outboxEntityMapper;

    @Override
    public OutboxModel save(OutboxModel outboxModel) {
        OutboxEntity entity = outboxEntityMapper.toEntity(outboxModel);
        OutboxEntity savedEntity = jpaOutboxRepository.save(entity);
        return outboxEntityMapper.toModel(savedEntity);
    }

    @Override
    public List<OutboxModel> findPendingEvents(int maxRetry, int limit) {
        return jpaOutboxRepository
            .findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
                OutboxModel.OutboxStatus.PENDING, maxRetry, PageRequest.ofSize(limit)
            )
            .stream()
            .map(outboxEntityMapper::toModel)
            .toList();
    }

    @Override
    public int deletePublishedEventsBefore(Instant before) {
        return jpaOutboxRepository.deleteByStatusAndPublishedAtBefore(
            OutboxModel.OutboxStatus.PUBLISHED, before
        );
    }
}
