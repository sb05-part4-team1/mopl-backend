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
            toParticipantIdOnly(entity.getParticipant()),
            toConversationIdOnly(entity.getConversation())
        );
    }

    public ReadStatusModel toModelWithParticipant(ReadStatusEntity entity) {
        if (entity == null) {
            return null;
        }

        return buildReadStatusModel(
            entity,
            userEntityMapper.toModel(entity.getParticipant()),
            toConversationIdOnly(entity.getConversation())
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
            .participant(userEntityMapper.toEntity(model.getParticipant()))
            .conversation(conversationEntityMapper.toEntity(model.getConversation()))
            .build();
    }

    private ReadStatusModel buildReadStatusModel(
        ReadStatusEntity entity,
        UserModel user,
        ConversationModel conversation
    ) {
        return ReadStatusModel.builder()
            .id(entity.getId())
            .createdAt(entity.getCreatedAt())
            .lastReadAt(entity.getLastReadAt())
            .participant(user)
            .conversation(conversation)
            .build();
    }

    private UserModel toParticipantIdOnly(UserEntity entity) {
        return entity != null
            ? UserModel.builder().id(entity.getId()).build()
            : null;
    }

    private ConversationModel toConversationIdOnly(ConversationEntity entity) {
        return entity != null
            ? ConversationModel.builder().id(entity.getId()).build()
            : null;
    }
}
