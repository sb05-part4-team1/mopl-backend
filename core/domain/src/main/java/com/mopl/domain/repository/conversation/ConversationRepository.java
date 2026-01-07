package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import java.util.UUID;

public interface ConversationRepository {

    ConversationModel save(ConversationModel conversationModel);

    ConversationModel get(UUID conversationId);

    ConversationModel findById(UUID conversationId);
}
