package com.mopl.domain.service.conversation;

import com.mopl.domain.exception.conversation.ReadStatusNotFoundException;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ReadStatusService {

    private final ReadStatusRepository readStatusRepository;

    public Map<UUID, ReadStatusModel> getOtherReadStatusWithUserByConversationIdIn(
        Collection<UUID> conversationIds,
        UUID requesterId
    ) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            return Map.of();
        }
        return readStatusRepository.findOthersByConversationIds(List.copyOf(conversationIds), requesterId);
    }

    public Map<UUID, ReadStatusModel> getMyReadStatusByConversationIdIn(
        Collection<UUID> conversationIds,
        UUID requesterId
    ) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            return Map.of();
        }
        return readStatusRepository.findMineByConversationIds(List.copyOf(conversationIds), requesterId);
    }

    public ReadStatusModel getOtherReadStatus(UUID conversationId, UUID requesterId) {
        return readStatusRepository.findOtherReadStatus(conversationId, requesterId);
    }

    public ReadStatusModel getMyReadStatus(UUID conversationId, UUID requesterId) {
        return readStatusRepository.findByConversationIdAndUserId(conversationId, requesterId);
    }

    public ReadStatusModel getReadStatusById(UUID readStatusId) {
        return readStatusRepository.findById(readStatusId)
            .orElseThrow(() -> ReadStatusNotFoundException.withId(readStatusId));
    }

    public void markAsRead(DirectMessageModel directMessageModel, ReadStatusModel readStatusModel) {
        if (directMessageModel != null) {
            readStatusModel.updateLastRead(Instant.now());
            readStatusRepository.save(readStatusModel);
        }
    }
}
