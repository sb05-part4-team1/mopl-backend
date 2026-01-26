package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.ReadStatusModel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository {

    List<ReadStatusModel> findByParticipantIdAndConversationIdIn(UUID participantId, Collection<UUID> conversationIds);

    List<ReadStatusModel> findWithParticipantByParticipantIdNotAndConversationIdIn(UUID participantId, Collection<UUID> conversationIds);

    Optional<ReadStatusModel> findByParticipantIdAndConversationId(UUID participantId, UUID conversationId);

    Optional<ReadStatusModel> findWithParticipantByParticipantIdAndConversationId(UUID participantId, UUID conversationId);

    Optional<ReadStatusModel> findWithParticipantByParticipantIdNotAndConversationId(UUID participantId, UUID conversationId);

    ReadStatusModel save(ReadStatusModel readStatusModel);
}
