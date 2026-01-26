package com.mopl.jpa.entity.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationEntityMapper {

    public ConversationModel toModel(ConversationEntity conversationEntity) {
        if (conversationEntity == null) {
            return null;
        }

        return ConversationModel.builder()
            .id(conversationEntity.getId())
            .createdAt(conversationEntity.getCreatedAt())
            .updatedAt(conversationEntity.getUpdatedAt())
            .build();
    }

    public ConversationEntity toEntity(ConversationModel conversationModel) {
        if (conversationModel == null) {
            return null;
        }

        return ConversationEntity.builder()
            .id(conversationModel.getId())
            .createdAt(conversationModel.getCreatedAt())
            .updatedAt(conversationModel.getUpdatedAt())
            .build();
    }
}
