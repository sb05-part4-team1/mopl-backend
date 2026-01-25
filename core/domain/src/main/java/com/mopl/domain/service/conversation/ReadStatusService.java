package com.mopl.domain.service.conversation;

import com.mopl.domain.exception.conversation.ConversationAccessDeniedException;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ReadStatusService {

    private final ReadStatusRepository readStatusRepository;

    public Map<UUID, ReadStatusModel> getMyReadStatusMapByConversationIdIn(
        UUID participantId,
        Collection<UUID> conversationIds
    ) {
        return readStatusRepository
            .findByParticipantIdAndConversationIdIn(participantId, conversationIds)
            .stream()
            .collect(Collectors.toMap(
                rs -> rs.getConversation().getId(),
                Function.identity()
            ));
    }

    public Map<UUID, ReadStatusModel> getOtherReadStatusWithParticipantMapByConversationIdIn(
        UUID participantId,
        Collection<UUID> conversationIds
    ) {
        return readStatusRepository
            .findWithParticipantByParticipantIdNotAndConversationIdIn(participantId, conversationIds)
            .stream()
            .collect(Collectors.toMap(
                rs -> rs.getConversation().getId(),
                Function.identity()
            ));
    }

    public ReadStatusModel getMyReadStatus(UUID conversationId, UUID participantId) {
        return readStatusRepository
            .findByParticipantIdAndConversationId(participantId, conversationId)
            .orElse(null);
    }

    public ReadStatusModel getOtherReadStatusWithParticipant(UUID conversationId, UUID participantId) {
        return readStatusRepository
            .findWithParticipantByParticipantIdNotAndConversationId(participantId, conversationId)
            .orElse(null);
    }

    public void validateParticipant(UUID participantId, UUID conversationId) {
        if (!readStatusRepository.existsByParticipantIdAndConversationId(participantId, conversationId)) {
            throw ConversationAccessDeniedException.withUserIdAndConversationId(participantId, conversationId);
        }
    }

    public void markAsRead(DirectMessageModel directMessageModel, ReadStatusModel readStatusModel) {
        if (directMessageModel != null) {
            ReadStatusModel updated = readStatusModel.markAsRead();
            readStatusRepository.save(updated);
        }
    }
}
