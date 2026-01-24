package com.mopl.domain.service.conversation;

import com.mopl.domain.exception.conversation.ConversationAccessDeniedException;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ReadStatusService {

    private final ReadStatusRepository readStatusRepository;

    public ReadStatusModel getOtherReadStatusWithParticipant(UUID userId, UUID conversationId) {
        return readStatusRepository.findWithParticipantByParticipantIdNotAndConversationId(userId, conversationId);
    }

    public ReadStatusModel getMyReadStatus(UUID conversationId, UUID requesterId) {
        return readStatusRepository.findByConversationIdAndUserId(conversationId, requesterId);
    }

    public Map<UUID, ReadStatusModel> getOtherReadStatusWithParticipantByConversationIdIn(
        UUID userId,
        Collection<UUID> conversationIds
    ) {
        return readStatusRepository.findOtherReadStatusWithParticipantByConversationIdIn(userId, conversationIds);
    }

    public Map<UUID, ReadStatusModel> getMyReadStatusByConversationIdIn(
        UUID userId,
        Collection<UUID> conversationIds
    ) {
        return readStatusRepository.findMyReadStatusByConversationIdIn(userId, conversationIds);
    }

    public void markAsRead(DirectMessageModel directMessageModel, ReadStatusModel readStatusModel) {
        if (directMessageModel != null) {
            ReadStatusModel updated = readStatusModel.markAsRead();
            readStatusRepository.save(updated);
        }
    }

    public void validateParticipant(UUID participantId, UUID conversationId) {
        if (!readStatusRepository.existsByParticipantIdAndConversationId(
            participantId,
            conversationId
        )) {
            throw ConversationAccessDeniedException.withUserIdAndConversationId(
                participantId,
                conversationId
            );
        }
    }
}
