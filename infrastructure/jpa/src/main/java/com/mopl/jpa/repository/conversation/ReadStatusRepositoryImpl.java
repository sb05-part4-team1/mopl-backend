package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import com.mopl.jpa.entity.conversation.ReadStatusEntity;
import com.mopl.jpa.entity.conversation.ReadStatusEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReadStatusRepositoryImpl implements ReadStatusRepository {

    private final JpaReadStatusRepository jpaReadStatusRepository;
    private final ReadStatusEntityMapper readStatusEntityMapper;

    @Override
    public ReadStatusModel findByConversationIdAndUserId(UUID conversationId, UUID userId) {
        ReadStatusEntity readStatusEntity = jpaReadStatusRepository
            .findByConversationIdAndParticipantId(conversationId, userId);

        return readStatusEntityMapper.toModel(readStatusEntity);
    }

    @Override
    public ReadStatusModel findWithParticipantByParticipantIdNotAndConversationId(UUID participantId, UUID conversationId) {
        ReadStatusEntity readStatusEntity = jpaReadStatusRepository
            .findWithParticipantByParticipantIdNotAndConversationId(participantId, conversationId);

        return readStatusEntityMapper.toModelWithUser(readStatusEntity);
    }

    @Override
    public Map<UUID, ReadStatusModel> findOtherReadStatusWithParticipantByConversationIdIn(
        UUID userId,
        Collection<UUID> conversationIds
    ) {
        return jpaReadStatusRepository
            .findWithParticipantByParticipantIdNotAndConversationIdIn(userId, conversationIds)
            .stream()
            .map(readStatusEntityMapper::toModelWithUser)
            .collect(Collectors.toMap(
                rs -> rs.getConversation().getId(),
                Function.identity()
            ));
    }

    @Override
    public Map<UUID, ReadStatusModel> findMyReadStatusByConversationIdIn(
        UUID userId,
        Collection<UUID> conversationIds
    ) {
        return jpaReadStatusRepository
            .findByParticipantIdAndConversationIdIn(userId, conversationIds)
            .stream()
            .map(readStatusEntityMapper::toModel)
            .collect(Collectors.toMap(
                rs -> rs.getConversation().getId(),
                Function.identity()
            ));
    }

    @Override
    public boolean existsByConversationIdAndParticipantId(UUID conversationId, UUID participantId) {
        return jpaReadStatusRepository.existsByConversationIdAndParticipantId(conversationId, participantId);
    }

    @Override
    public ReadStatusModel save(ReadStatusModel readStatusModel) {
        ReadStatusEntity readStatusEntity = readStatusEntityMapper.toEntity(readStatusModel);
        ReadStatusEntity savedReadStatusEntity = jpaReadStatusRepository.save(readStatusEntity);

        return readStatusEntityMapper.toModel(savedReadStatusEntity);
    }
}
