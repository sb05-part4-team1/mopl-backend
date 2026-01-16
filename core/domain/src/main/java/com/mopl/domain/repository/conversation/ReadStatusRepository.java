package com.mopl.domain.repository.conversation;

import com.mopl.domain.model.conversation.ReadStatusModel;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository {

    ReadStatusModel save(ReadStatusModel readStatusModel);

    Optional<ReadStatusModel> findById(UUID readStatusId);

    List<ReadStatusModel> findByConversationId(UUID conversationId);

    ReadStatusModel findByConversationIdAndParticipantId(UUID conversationId, UUID participantId);

    List<ReadStatusModel> findByParticipantId(UUID participantId);

    ReadStatusModel findByConversationIdAndUserId(UUID conversationId, UUID userId);

    ReadStatusModel findOtherReadStatus(UUID conversationId, UUID userId);

    Map<UUID, ReadStatusModel> findOthersByConversationIds(List<UUID> conversationIds, UUID userId);

    Map<UUID, ReadStatusModel> findMineByConversationIds(List<UUID> conversationIds, UUID userId);
}
