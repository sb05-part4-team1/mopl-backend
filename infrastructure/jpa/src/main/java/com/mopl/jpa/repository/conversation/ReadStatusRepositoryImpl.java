package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import com.mopl.jpa.entity.conversation.ReadStatusEntity;
import com.mopl.jpa.entity.conversation.ReadStatusEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReadStatusRepositoryImpl implements ReadStatusRepository {

    private final JpaReadStatusRepository jpaReadStatusRepository;
    private final ReadStatusEntityMapper readStatusEntityMapper;

    @Override
    public List<ReadStatusModel> findByParticipantIdAndConversationIdIn(
        UUID participantId,
        Collection<UUID> conversationIds
    ) {
        return jpaReadStatusRepository
            .findByParticipantIdAndConversationIdIn(participantId, conversationIds)
            .stream()
            .map(readStatusEntityMapper::toModel)
            .toList();
    }

    @Override
    public List<ReadStatusModel> findWithParticipantByParticipantIdNotAndConversationIdIn(
        UUID participantId,
        Collection<UUID> conversationIds
    ) {
        return jpaReadStatusRepository
            .findWithParticipantByParticipantIdNotAndConversationIdIn(participantId, conversationIds)
            .stream()
            .map(readStatusEntityMapper::toModelWithParticipant)
            .toList();
    }

    @Override
    public Optional<ReadStatusModel> findByParticipantIdAndConversationId(UUID participantId, UUID conversationId) {
        return jpaReadStatusRepository
            .findByParticipantIdAndConversationId(participantId, conversationId)
            .map(readStatusEntityMapper::toModel);
    }

    @Override
    public Optional<ReadStatusModel> findWithParticipantByParticipantIdNotAndConversationId(
        UUID participantId,
        UUID conversationId
    ) {
        return jpaReadStatusRepository
            .findWithParticipantByParticipantIdNotAndConversationId(participantId, conversationId)
            .map(readStatusEntityMapper::toModelWithParticipant);
    }

    @Override
    public boolean existsByParticipantIdAndConversationId(UUID participantId, UUID conversationId) {
        return jpaReadStatusRepository.existsByParticipantIdAndConversationId(participantId, conversationId);
    }

    @Override
    public ReadStatusModel save(ReadStatusModel readStatusModel) {
        ReadStatusEntity readStatusEntity = readStatusEntityMapper.toEntity(readStatusModel);
        ReadStatusEntity savedReadStatusEntity = jpaReadStatusRepository.save(readStatusEntity);
        return readStatusEntityMapper.toModel(savedReadStatusEntity);
    }
}
