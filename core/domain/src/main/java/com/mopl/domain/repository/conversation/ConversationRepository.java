package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository {

    Optional<ConversationModel> findById(UUID conversationId);

    ConversationModel save(ConversationModel conversationModel);
}
