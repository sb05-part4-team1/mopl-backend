package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository {

    ConversationModel save(ConversationModel conversationModel);

    Optional<ConversationModel> get(UUID conversationId);

    Optional<ConversationModel> findById(UUID conversationId);
}
