package com.mopl.jpa.entity.conversation;

import com.mopl.domain.model.conversation.ReadStatusModel;
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
            .lastReadAt(readStatusModel.getLastRead())
            .conversation(conversationEntityMapper.toEntity(readStatusModel.getConversation()))
            .participant(userEntityMapper.toEntity(readStatusModel.getUser()))
            .build();

    }

    public ReadStatusModel toModel(ReadStatusEntity readStatusEntity) {

        if (readStatusEntity == null) {
            return null;
        }

        return ReadStatusModel.builder()
            .id(readStatusEntity.getId())
            .createdAt(readStatusEntity.getCreatedAt())
            .lastRead(readStatusEntity.getLastReadAt())
            .conversation(conversationEntityMapper.toModel(readStatusEntity.getConversation()))
            .user(userEntityMapper.toModel(readStatusEntity.getParticipant()))
            .build();
    }
}
