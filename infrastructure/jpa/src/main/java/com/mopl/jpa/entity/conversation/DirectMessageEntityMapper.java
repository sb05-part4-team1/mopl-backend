package com.mopl.jpa.entity.conversation;

import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.jpa.entity.user.UserEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessageEntityMapper {

    private final EntityManager em;

    public DirectMessageModel toModel(DirectMessageEntity directMessageEntity) {

        if (directMessageEntity == null) {
            return null;
        }

        return DirectMessageModel.builder()
            .id(directMessageEntity.getId())
            .conversationId(directMessageEntity.getConversation().getId())
            .senderId(directMessageEntity.getSender().getId())
            .createdAt(directMessageEntity.getCreatedAt())
            .content(directMessageEntity.getContent())
            .build();
    }

    public DirectMessageEntity toEntity(DirectMessageModel directMessageModel) {
        if (directMessageModel == null) {
            return null;
        }

        ConversationEntity conversation = em.getReference(ConversationEntity.class,
            directMessageModel.getConversationId());

        UserEntity sender = em.getReference(UserEntity.class, directMessageModel.getSenderId());

        return DirectMessageEntity.builder()
            .id(directMessageModel.getId())
            .conversation(conversation)
            .sender(sender)
            .createdAt(directMessageModel.getCreatedAt())
            .content(directMessageModel.getContent())
            .build();

    }

}
