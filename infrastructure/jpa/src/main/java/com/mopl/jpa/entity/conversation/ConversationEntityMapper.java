package com.mopl.jpa.entity.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.user.UserModel;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationEntityMapper {

//    withUser, lastMessage, hasUnread을 매핑하는 건 일단 보류

    //tomodel에서는 추후에 repository, 다른 mapper들 사용해서 매핑하기로
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

    public ConversationModel toModel(
        ConversationEntity conversationEntity,
        UserModel userModel
    ) {

        if (conversationEntity == null) {
            return null;
        }

        return ConversationModel.builder()
            .id(conversationEntity.getId())
            .createdAt(conversationEntity.getCreatedAt())
            .updatedAt(conversationEntity.getUpdatedAt())
            .withUser(userModel)
            .build();
    }

    public ConversationModel toModel(
        ConversationEntity conversationEntity,
        Optional<DirectMessageModel> directMessageModel
    ) {

        if (conversationEntity == null) {
            return null;
        }

        return ConversationModel.builder()
            .id(conversationEntity.getId())
            .createdAt(conversationEntity.getCreatedAt())
            .updatedAt(conversationEntity.getUpdatedAt())
            .lastMessage(directMessageModel.orElse(null))
            .build();

    }

    public ConversationModel toModel(
        ConversationEntity conversationEntity,
        DirectMessageModel directMessageModel,
        UserModel userModel
    ) {

        if (conversationEntity == null) {
            return null;
        }

        return ConversationModel.builder()
            .id(conversationEntity.getId())
            .createdAt(conversationEntity.getCreatedAt())
            .updatedAt(conversationEntity.getUpdatedAt())
            .withUser(userModel)
            .lastMessage(directMessageModel)
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
