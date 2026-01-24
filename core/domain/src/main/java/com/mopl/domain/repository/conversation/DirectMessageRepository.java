package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.DirectMessageModel;

import java.util.Optional;
import java.util.UUID;

public interface DirectMessageRepository {

    Optional<DirectMessageModel> findOtherDirectMessage(
        UUID conversationId,
        UUID directMessageId,
        UUID userId
    );

    Optional<DirectMessageModel> findLastMessageByConversationId(UUID conversationId);

    DirectMessageModel save(DirectMessageModel directMessageModel);
}
