package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.DirectMessageModel;
import java.util.Optional;
import java.util.UUID;

public interface DirectMessageRepository {

    DirectMessageModel save(DirectMessageModel directMessageModel);

    Optional<DirectMessageModel> findById(UUID conversationId);

    DirectMessageModel findByConversationIdAndSenderId(UUID conversationId, UUID senderId);
}
