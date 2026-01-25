package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.support.cursor.CursorResponse;

import java.util.Optional;
import java.util.UUID;

public interface ConversationQueryRepository {

    CursorResponse<ConversationModel> findAll(UUID userId, ConversationQueryRequest request);

    Optional<ConversationModel> findByParticipants(UUID userId, UUID withId);
}
