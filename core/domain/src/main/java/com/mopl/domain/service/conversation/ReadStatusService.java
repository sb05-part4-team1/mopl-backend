package com.mopl.domain.service.conversation;

import com.mopl.domain.exception.conversation.ConversationAccessDeniedException;
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

    public Map<UUID, ReadStatusModel> getMyReadStatusMap(
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

    public Map<UUID, ReadStatusModel> getOtherReadStatusMapWithParticipant(
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

    public ReadStatusModel getMyReadStatus(UUID participantId, UUID conversationId) {
        return readStatusRepository
            .findByParticipantIdAndConversationId(participantId, conversationId)
            .orElse(null);
    }

    public ReadStatusModel getOtherReadStatusWithParticipant(UUID participantId, UUID conversationId) {
        return readStatusRepository
            .findWithParticipantByParticipantIdNotAndConversationId(participantId, conversationId)
            .orElse(null);
    }

    public ReadStatusModel create(ReadStatusModel readStatusModel) {
        return readStatusRepository.save(readStatusModel);
    }

    public ReadStatusModel update(ReadStatusModel readStatusModel) {
        return readStatusRepository.save(readStatusModel);
    }

    public void validateParticipant(UUID participantId, UUID conversationId) {
        if (!readStatusRepository.existsByParticipantIdAndConversationId(participantId, conversationId)) {
            throw ConversationAccessDeniedException.withUserIdAndConversationId(participantId, conversationId);
        }
    }
}
