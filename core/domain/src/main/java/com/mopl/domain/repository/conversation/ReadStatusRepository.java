package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.ReadStatusModel;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface ReadStatusRepository {

    ReadStatusModel findByConversationIdAndUserId(UUID conversationId, UUID userId);

    ReadStatusModel findWithParticipantByParticipantIdNotAndConversationId(UUID userId, UUID conversationId);

    Map<UUID, ReadStatusModel> findOtherReadStatusWithParticipantByConversationIdIn(UUID userId, Collection<UUID> conversationIds);

    Map<UUID, ReadStatusModel> findMyReadStatusByConversationIdIn(UUID userId, Collection<UUID> conversationIds);

    boolean existsByConversationIdAndParticipantId(UUID conversationId, UUID participantId);

    ReadStatusModel save(ReadStatusModel readStatusModel);
}
