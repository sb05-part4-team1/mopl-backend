package com.mopl.domain.service.conversation;

import com.mopl.domain.exception.conversation.ConversationNotFoundException;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.repository.conversation.ConversationQueryRepository;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ConversationService {

    private final ConversationQueryRepository conversationQueryRepository;
    private final ConversationRepository conversationRepository;

    public CursorResponse<ConversationModel> getAll(UUID userId, ConversationQueryRequest request) {
        return conversationQueryRepository.findAll(userId, request);
    }

    public ConversationModel getById(UUID conversationId) {
        return conversationRepository.findById(conversationId)
            .orElseThrow(() -> ConversationNotFoundException.withId(conversationId));
    }

    public ConversationModel create(ConversationModel conversationModel) {
        return conversationRepository.save(conversationModel);
    }

    public ConversationModel getByParticipants(UUID userId, UUID withUserId) {
        return conversationQueryRepository.findByParticipants(userId, withUserId)
            .orElseThrow(() -> ConversationNotFoundException.withParticipants(userId, withUserId));
    }

    public boolean existsByParticipants(UUID userId, UUID withUserId) {
        return conversationQueryRepository.existsByParticipants(userId, withUserId);
    }
}
