package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.jpa.entity.conversation.ConversationEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepository {

    private final JpaConversationRepository jpaConversationRepository;
    private final ConversationEntityMapper conversationEntityMapper;

    @Override
    public Optional<ConversationModel> findById(UUID conversationId) {
        return jpaConversationRepository.findById(conversationId)
            .map(conversationEntityMapper::toModel);
    }

    @Override
    public ConversationModel save(ConversationModel conversationModel) {
        ConversationEntity conversationEntity = conversationEntityMapper.toEntity(conversationModel);
        ConversationEntity savedConversationEntity = jpaConversationRepository.save(conversationEntity);
        return conversationEntityMapper.toModel(savedConversationEntity);
    }
}
