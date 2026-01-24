package com.mopl.domain.service.conversation;

import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ReadStatusService {

    private final ReadStatusRepository readStatusRepository;

    public Map<UUID, ReadStatusModel> getOtherReadStatusWithParticipantByConversationIdIn(
        UUID userId,
        Collection<UUID> conversationIds
    ) {
        return readStatusRepository.findOtherReadStatusWithParticipantByConversationIdIn(userId, conversationIds);
    }

    public Map<UUID, ReadStatusModel> getMyReadStatusWithParticipantByConversationIdIn(
        UUID userId,
        Collection<UUID> conversationIds
    ) {
        return readStatusRepository.findMyReadStatusWithParticipantByConversationIdIn(userId, conversationIds);
    }

    public ReadStatusModel getOtherReadStatus(UUID conversationId, UUID requesterId) {
        return readStatusRepository.findOtherReadStatus(conversationId, requesterId);
    }

    public ReadStatusModel getMyReadStatus(UUID conversationId, UUID requesterId) {
        return readStatusRepository.findByConversationIdAndUserId(conversationId, requesterId);
    }

    public void markAsRead(DirectMessageModel directMessageModel, ReadStatusModel readStatusModel) {
        if (directMessageModel != null) {
            ReadStatusModel updated = readStatusModel.markAsRead();
            readStatusRepository.save(updated);
        }
    }
}
