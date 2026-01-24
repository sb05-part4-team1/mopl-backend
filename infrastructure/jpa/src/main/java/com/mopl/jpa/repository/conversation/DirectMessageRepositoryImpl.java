package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import com.mopl.jpa.entity.conversation.DirectMessageEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DirectMessageRepositoryImpl implements DirectMessageRepository {

    private final JpaDirectMessageRepository jpaDirectMessageRepository;
    private final DirectMessageEntityMapper directMessageEntityMapper;

    @Override
    public DirectMessageModel save(DirectMessageModel directMessageModel) {
        DirectMessageEntity directMessageEntity = directMessageEntityMapper.toEntity(directMessageModel);
        DirectMessageEntity savedDirectMessageEntity = jpaDirectMessageRepository.save(directMessageEntity);
        return directMessageEntityMapper.toModel(savedDirectMessageEntity);
    }

    @Override
    public Optional<DirectMessageModel> findById(UUID directMessageId) {
        return jpaDirectMessageRepository.findById(directMessageId)
            .map(directMessageEntityMapper::toModel);
    }

    @Override
    public DirectMessageModel findByConversationIdAndSenderId(UUID conversationId, UUID senderId) {
        DirectMessageEntity directMessageEntity = jpaDirectMessageRepository
            .findTopByConversationIdAndSenderIdOrderByCreatedAtDesc(conversationId, senderId);
        return directMessageEntityMapper.toModel(directMessageEntity);
    }

    @Override
    public Optional<DirectMessageModel> findOtherDirectMessage(
        UUID conversationId,
        UUID directMessageId,
        UUID userId
    ) {
        return jpaDirectMessageRepository.findOther(conversationId, directMessageId, userId)
            .map(directMessageEntityMapper::toModel);
    }

    @Override
    public Optional<DirectMessageModel> findLastMessageByConversationId(UUID conversationId) {
        return jpaDirectMessageRepository.findTopByConversationIdOrderByCreatedAtDesc(conversationId)
            .map(directMessageEntityMapper::toModel);
    }

    @Override
    public Map<UUID, DirectMessageModel> findLastMessagesByConversationIdIn(Collection<UUID> conversationIds) {
        if (conversationIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, DirectMessageModel> result = new HashMap<>();
        for (UUID conversationId : conversationIds) {
            jpaDirectMessageRepository.findTopByConversationIdOrderByCreatedAtDesc(conversationId)
                .ifPresent(entity -> result.put(conversationId, directMessageEntityMapper.toModel(entity)));
        }

        return result;
    }
}
