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
            toConversationIdOnly(entity.getConversation()),
            toSenderIdOnly(entity.getSender())
        );
    }

    public DirectMessageModel toModelWithSender(DirectMessageEntity entity) {
        if (entity == null) {
            return null;
        }

        return buildDirectMessageModel(
            entity,
            toConversationIdOnly(entity.getConversation()),
            userEntityMapper.toModel(entity.getSender())
        );
    }

    public DirectMessageEntity toEntity(DirectMessageModel model) {
        if (model == null) {
            return null;
        }

        return DirectMessageEntity.builder()
            .id(model.getId())
            .conversation(conversationEntityMapper.toEntity(model.getConversation()))
            .sender(userEntityMapper.toEntity(model.getSender()))
            .createdAt(model.getCreatedAt())
            .content(model.getContent())
            .build();
    }

    private DirectMessageModel buildDirectMessageModel(
        DirectMessageEntity entity,
        ConversationModel conversation,
        UserModel sender
    ) {
        return DirectMessageModel.builder()
            .id(entity.getId())
            .createdAt(entity.getCreatedAt())
            .deletedAt(entity.getDeletedAt())
            .conversation(conversation)
            .sender(sender)
            .content(entity.getContent())
            .build();
    }

    private ConversationModel toConversationIdOnly(ConversationEntity entity) {
        return entity != null
            ? ConversationModel.builder().id(entity.getId()).build()
            : null;
    }

    private UserModel toSenderIdOnly(UserEntity entity) {
        return entity != null
            ? UserModel.builder().id(entity.getId()).build()
            : null;
    }
}
