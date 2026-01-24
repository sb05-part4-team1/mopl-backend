package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.ReadStatusModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReadStatusRepository {

    ReadStatusModel findByConversationIdAndUserId(UUID conversationId, UUID userId);

    ReadStatusModel findOtherReadStatus(UUID conversationId, UUID userId);

    Map<UUID, ReadStatusModel> findOtherReadStatusWithParticipantByConversationIdIn(UUID userId, Collection<UUID> conversationIds);

    Map<UUID, ReadStatusModel> findMyReadStatusWithParticipantByConversationIdIn(UUID userId, Collection<UUID> conversationIds);

    ReadStatusModel save(ReadStatusModel readStatusModel);
}
