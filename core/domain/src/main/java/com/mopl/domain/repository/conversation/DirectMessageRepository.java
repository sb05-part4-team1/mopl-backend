package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.DirectMessageModel;

import java.util.Optional;
import java.util.UUID;

public interface DirectMessageRepository {

    Optional<DirectMessageModel> findLastMessageByConversationId(UUID conversationId);

    Optional<DirectMessageModel> findLastMessageWithSenderByConversationId(UUID conversationId);

    DirectMessageModel save(DirectMessageModel directMessageModel);
}
