package com.mopl.jpa.entity.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessageEntityMapper {

    private final ConversationEntityMapper conversationEntityMapper;
    private final UserEntityMapper userEntityMapper;

    public DirectMessageModel toModel(DirectMessageEntity entity) {
        if (entity == null) {
            return null;
        }

        return buildDirectMessageModel(
            entity,
            toSenderIdOnly(entity.getSender()),
            toConversationIdOnly(entity.getConversation())
        );
    }

    public DirectMessageModel toModelWithSender(DirectMessageEntity entity) {
        if (entity == null) {
            return null;
        }

        return buildDirectMessageModel(
            entity,
            toSenderIfActive(entity.getSender()),
            toConversationIdOnly(entity.getConversation())
        );
    }

    public DirectMessageEntity toEntity(DirectMessageModel model) {
        if (model == null) {
            return null;
        }

        return DirectMessageEntity.builder()
            .id(model.getId())
            .createdAt(model.getCreatedAt())
            .content(model.getContent())
            .sender(userEntityMapper.toEntity(model.getSender()))
            .conversation(conversationEntityMapper.toEntity(model.getConversation()))
            .build();
    }

    private DirectMessageModel buildDirectMessageModel(
        DirectMessageEntity entity,
        UserModel sender,
        ConversationModel conversation
    ) {
        return DirectMessageModel.builder()
            .id(entity.getId())
            .createdAt(entity.getCreatedAt())
            .deletedAt(entity.getDeletedAt())
            .sender(sender)
            .conversation(conversation)
            .content(entity.getContent())
            .build();
    }

    private UserModel toSenderIdOnly(UserEntity entity) {
        return isActiveUser(entity)
            ? UserModel.builder().id(entity.getId()).build()
            : null;
    }

    private UserModel toSenderIfActive(UserEntity entity) {
        return isActiveUser(entity)
            ? userEntityMapper.toModel(entity)
            : null;
    }

    private boolean isActiveUser(UserEntity entity) {
        return entity != null && entity.getDeletedAt() == null;
    }

    private ConversationModel toConversationIdOnly(ConversationEntity entity) {
        return entity != null
            ? ConversationModel.builder().id(entity.getId()).build()
            : null;
    }
}
