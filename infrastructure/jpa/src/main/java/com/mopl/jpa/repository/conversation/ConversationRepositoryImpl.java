package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.jpa.entity.conversation.ConversationEntityMapper;
import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import com.mopl.jpa.entity.conversation.DirectMessageEntityMapper;
import java.util.Optional;
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
    public Optional<ConversationModel> get(UUID conversationId) {
        Optional<ConversationEntity> conversationEntity = jpaConversationRepository.findById(conversationId);

        Optional<DirectMessageEntity> directMessageEntity = jpaDirectMessageRepository
            .findTopByConversationIdOrderByCreatedAtDesc(conversationId);


        return conversationEntity.map(entity ->
                        conversationEntityMapper.toModel(
                                entity,
                                directMessageEntity.map(directMessageEntityMapper::toModel)
                        )
                );
    }



    @Override
    public Optional<ConversationModel> findById(UUID conversationId) {

        return  jpaConversationRepository.findById(conversationId)
                .map(conversationEntityMapper::toModel);

    }

}
