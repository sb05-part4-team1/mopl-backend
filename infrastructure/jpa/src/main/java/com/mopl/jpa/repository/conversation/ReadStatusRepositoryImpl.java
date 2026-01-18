package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import com.mopl.jpa.entity.conversation.ReadStatusEntity;
import com.mopl.jpa.entity.conversation.ReadStatusEntityMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReadStatusRepositoryImpl implements ReadStatusRepository {

    private final ReadStatusEntityMapper readStatusEntityMapper;
    private final JpaReadStatusRepository jpaReadStatusRepository;

    @Override
    public ReadStatusModel save(ReadStatusModel readStatusModel) {
        ReadStatusEntity readStatusEntity = readStatusEntityMapper.toEntity(readStatusModel);
        ReadStatusEntity savedReadStatusEntity = jpaReadStatusRepository.save(readStatusEntity);

        return readStatusEntityMapper.toModel(savedReadStatusEntity);
    }

    @Override
    public Optional<ReadStatusModel> findById(UUID readStatusId) {

        return jpaReadStatusRepository.findById(readStatusId)
            .map(readStatusEntityMapper::toModel);
    }

    @Override
    public List<ReadStatusModel> findByConversationId(UUID conversationId) {
        List<ReadStatusEntity> readStatusEntities = jpaReadStatusRepository.findByConversationId(
            conversationId);

        return readStatusEntities.stream()
            .map(readStatusEntityMapper::toModel)
            .collect(Collectors.toList());
    }

    @Override
    public ReadStatusModel findByConversationIdAndParticipantId(
        UUID conversationId,
        UUID participantId
    ) {
        ReadStatusEntity readStatusEntity = jpaReadStatusRepository
            .findByConversationIdAndParticipantId(conversationId, participantId);

        return readStatusEntityMapper.toModel(readStatusEntity);
    }

    @Override
    public List<ReadStatusModel> findByParticipantId(UUID participantId) {
        List<ReadStatusEntity> readStatusEntities = jpaReadStatusRepository.findByParticipantId(
            participantId);

        return readStatusEntities.stream()
            .map(readStatusEntityMapper::toModel)
            .collect(Collectors.toList());
    }

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

        return readStatusEntityMapper.toModel(readStatusEntity);
    }

    @Override
    public Map<UUID, ReadStatusModel> findOthersByConversationIds(List<UUID> conversationIds,
        UUID userId) {
        return jpaReadStatusRepository
            .findOthersByConversationIds(conversationIds, userId)
            .stream()
            .map(readStatusEntityMapper::toModel)
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

}
