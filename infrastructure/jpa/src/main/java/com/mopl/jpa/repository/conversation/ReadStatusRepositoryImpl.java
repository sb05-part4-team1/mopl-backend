package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import com.mopl.jpa.entity.conversation.ReadStatusEntity;
import com.mopl.jpa.entity.conversation.ReadStatusEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
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
    public ReadStatusModel findOtherReadStatus(UUID conversationId, UUID userId) {
        ReadStatusEntity readStatusEntity = jpaReadStatusRepository
            .findOtherReadStatus(conversationId, userId);

        return readStatusEntityMapper.toModelWithUser(readStatusEntity);
    }

    @Override
    public Map<UUID, ReadStatusModel> findOtherReadStatusWithUserByConversationIdIn(
        UUID userId,
        Collection<UUID> conversationIds
    ) {
        return jpaReadStatusRepository
            .findOthersByConversationIds(userId, conversationIds)
            .stream()
            .map(readStatusEntityMapper::toModelWithUser)
            .collect(Collectors.toMap(
                rs -> rs.getConversation().getId(),
                Function.identity()
            ));
    }

    @Override
    public Map<UUID, ReadStatusModel> findMineByConversationIds(List<UUID> conversationIds,
        UUID userId) {
        return jpaReadStatusRepository
            .findMineByConversationIds(conversationIds, userId)
            .stream()
            .map(readStatusEntityMapper::toModel)
            .collect(Collectors.toMap(
                rs -> rs.getConversation().getId(),
                Function.identity()
            ));
    }

    @Override
    public ReadStatusModel save(ReadStatusModel readStatusModel) {
        ReadStatusEntity readStatusEntity = readStatusEntityMapper.toEntity(readStatusModel);
        ReadStatusEntity savedReadStatusEntity = jpaReadStatusRepository.save(readStatusEntity);

        return readStatusEntityMapper.toModel(savedReadStatusEntity);
    }
}
