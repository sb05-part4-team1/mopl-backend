package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import com.mopl.jpa.entity.conversation.ReadStatusEntity;
import com.mopl.jpa.entity.conversation.ReadStatusEntityMapper;
import java.util.List;
import java.util.UUID;
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
    public ReadStatusModel findById(UUID readStatusId) {
        ReadStatusEntity readStatusEntity = jpaReadStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new IllegalArgumentException("ReadStatus not found"));

        return readStatusEntityMapper.toModel(readStatusEntity);
    }

    @Override
    public List<ReadStatusModel> findByConversationId(UUID conversationId) {
        List<ReadStatusEntity> readStatusEntities =
                jpaReadStatusRepository.findByConversationId(conversationId);

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
                .findByConversationIdAndParticipantId(conversationId,participantId);

        return readStatusEntityMapper.toModel(readStatusEntity);
    }


}
