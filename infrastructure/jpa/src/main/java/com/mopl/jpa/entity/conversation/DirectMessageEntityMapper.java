package com.mopl.jpa.entity.conversation;

import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessageEntityMapper {

    private final ConversationEntityMapper conversationMapper;
    private final UserEntityMapper userMapper;

    public DirectMessageModel toModel(DirectMessageEntity directMessageEntity) {

        if (directMessageEntity == null) {
            return null;
        }

        return DirectMessageModel.builder()
            .id(directMessageEntity.getId())
            .conversation(conversationMapper.toModel(directMessageEntity.getConversation()))
            .sender(userMapper.toModel(directMessageEntity.getSender()))
            .createdAt(directMessageEntity.getCreatedAt())
            .content(directMessageEntity.getContent())
            .build();
    }

    public DirectMessageEntity toEntity(DirectMessageModel directMessageModel) {
        if (directMessageModel == null) {
            return null;
        }

        return DirectMessageEntity.builder()
            .id(directMessageModel.getId())
            .conversation(conversationMapper.toEntity(directMessageModel.getConversation()))
            .sender(userMapper.toEntity(directMessageModel.getSender()))
            .createdAt(directMessageModel.getCreatedAt())
            .content(directMessageModel.getContent())
            .build();

    }

}
