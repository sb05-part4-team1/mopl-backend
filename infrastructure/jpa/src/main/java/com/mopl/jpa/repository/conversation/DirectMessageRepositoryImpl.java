package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import com.mopl.jpa.entity.conversation.DirectMessageEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DirectMessageRepositoryImpl implements DirectMessageRepository {

    private final JpaDirectMessageRepository jpaDirectMessageRepository;
    private final DirectMessageEntityMapper directMessageEntityMapper;

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
    public DirectMessageModel save(DirectMessageModel directMessageModel) {
        DirectMessageEntity directMessageEntity = directMessageEntityMapper.toEntity(directMessageModel);
        DirectMessageEntity savedDirectMessageEntity = jpaDirectMessageRepository.save(directMessageEntity);
        return directMessageEntityMapper.toModelWithSender(savedDirectMessageEntity);
    }
}
