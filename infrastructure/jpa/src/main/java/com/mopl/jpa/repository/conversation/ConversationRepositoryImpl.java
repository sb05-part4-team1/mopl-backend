package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.jpa.entity.conversation.ConversationEntityMapper;
import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import com.mopl.jpa.entity.conversation.DirectMessageEntityMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepository {

    private final JpaConversationRepository jpaConversationRepository;
    private final JpaDirectMessageRepository jpaDirectMessageRepository;
    private final ConversationEntityMapper conversationEntityMapper;
    private final DirectMessageEntityMapper directMessageEntityMapper;

    @Override
    public ConversationModel save(ConversationModel conversationModel) {
        ConversationEntity conversationEntity = conversationEntityMapper.toEntity(
            conversationModel);
        ConversationEntity savedConversationEntity = jpaConversationRepository.save(
            conversationEntity);
        return conversationEntityMapper.toModel(savedConversationEntity);
    }

    @Override
    public ConversationModel get(UUID conversationId) {
        ConversationEntity conversationEntity = jpaConversationRepository.findById(conversationId)
            .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        DirectMessageEntity directMessageEntity = jpaDirectMessageRepository
            .findTopByConversationIdOrderByCreatedAtDesc(conversationId)
            .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        return conversationEntityMapper
            .toModel(conversationEntity, directMessageEntityMapper.toModel(directMessageEntity));
    }

    @Override
    public ConversationModel findById(UUID conversationId) {
        ConversationEntity conversationEntity = jpaConversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        return conversationEntityMapper.toModel(conversationEntity);
    }

}
