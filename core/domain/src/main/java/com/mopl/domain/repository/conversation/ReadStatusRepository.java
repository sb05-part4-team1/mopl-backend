package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.ReadStatusModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReadStatusRepository {

    ReadStatusModel findByConversationIdAndUserId(UUID conversationId, UUID userId);

    ReadStatusModel findOtherReadStatus(UUID conversationId, UUID userId);

    Map<UUID, ReadStatusModel> findOtherReadStatusWithUserByConversationIdIn(UUID userId, Collection<UUID> conversationIds);

    Map<UUID, ReadStatusModel> findMineByConversationIds(List<UUID> conversationIds, UUID userId);

    ReadStatusModel save(ReadStatusModel readStatusModel);
}
