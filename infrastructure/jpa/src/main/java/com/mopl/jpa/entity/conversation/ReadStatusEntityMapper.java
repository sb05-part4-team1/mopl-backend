package com.mopl.jpa.entity.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadStatusEntityMapper {

    private final ConversationEntityMapper conversationEntityMapper;
    private final UserEntityMapper userEntityMapper;

    public ReadStatusModel toModel(ReadStatusEntity entity) {
        if (entity == null) {
            return null;
        }

        return buildReadStatusModel(
            entity,
            toConversationIdOnly(entity.getConversation()),
            toUserIdOnly(entity.getParticipant())
        );
    }

    public ReadStatusModel toModelWithUser(ReadStatusEntity entity) {
        if (entity == null) {
            return null;
        }

        return buildReadStatusModel(
            entity,
            toConversationIdOnly(entity.getConversation()),
            userEntityMapper.toModel(entity.getParticipant())
        );
    }

    public ReadStatusEntity toEntity(ReadStatusModel model) {
        if (model == null) {
            return null;
        }

        return ReadStatusEntity.builder()
            .id(model.getId())
            .createdAt(model.getCreatedAt())
            .lastReadAt(model.getLastReadAt())
            .conversation(conversationEntityMapper.toEntity(model.getConversation()))
            .participant(userEntityMapper.toEntity(model.getUser()))
            .build();
    }

    private ReadStatusModel buildReadStatusModel(
        ReadStatusEntity entity,
        ConversationModel conversation,
        UserModel user
    ) {
        return ReadStatusModel.builder()
            .id(entity.getId())
            .createdAt(entity.getCreatedAt())
            .lastReadAt(entity.getLastReadAt())
            .conversation(conversation)
            .user(user)
            .build();
    }

    private ConversationModel toConversationIdOnly(ConversationEntity entity) {
        return entity != null
            ? ConversationModel.builder().id(entity.getId()).build()
            : null;
    }

    private UserModel toUserIdOnly(UserEntity entity) {
        return entity != null
            ? UserModel.builder().id(entity.getId()).build()
            : null;
    }
}
