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

    public ReadStatusEntity toEntity(ReadStatusModel readStatusModel) {
        if (readStatusModel == null) {
            return null;
        }

        return ReadStatusEntity.builder()
            .id(readStatusModel.getId())
            .createdAt(readStatusModel.getCreatedAt())
            .lastReadAt(readStatusModel.getLastReadAt())
            .conversation(conversationEntityMapper.toEntity(readStatusModel.getConversation()))
            .participant(userEntityMapper.toEntity(readStatusModel.getUser()))
            .build();
    }

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
        if (entity == null) {
            return null;
        }
        return ConversationModel.builder()
            .id(entity.getId())
            .build();
    }

    private UserModel toUserIdOnly(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserModel.builder()
            .id(entity.getId())
            .build();
    }
}
