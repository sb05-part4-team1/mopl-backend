package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import com.mopl.jpa.entity.conversation.DirectMessageEntityMapper;
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
public class DirectMessageRepositoryImpl implements DirectMessageRepository {

    private final JpaDirectMessageRepository jpaDirectMessageRepository;
    private final DirectMessageEntityMapper directMessageEntityMapper;

    @Override
    public DirectMessageModel save(DirectMessageModel directMessageModel) {
        DirectMessageEntity directMessageEntity = directMessageEntityMapper.toEntity(
            directMessageModel);
        DirectMessageEntity savedDirectMessageEntity = jpaDirectMessageRepository.save(
            directMessageEntity);
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
        return jpaDirectMessageRepository.findTopByConversationIdOrderByCreatedAtDesc(
            conversationId)
            .map(directMessageEntityMapper::toModel);
    }

    @Override
    public Map<UUID, DirectMessageModel> findLastMessagesByConversationIds(
        List<UUID> conversationIds) {
        return jpaDirectMessageRepository
            .findLastMessagesByConversationIds(conversationIds)
            .stream()
            .map(directMessageEntityMapper::toModel)
            .collect(Collectors.toMap(
                dm -> dm.getConversation().getId(),
                Function.identity()
            ));
    }

}
