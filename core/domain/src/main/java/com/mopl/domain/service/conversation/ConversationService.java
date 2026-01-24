package com.mopl.domain.service.conversation;

import com.mopl.domain.exception.conversation.ConversationNotFoundException;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.conversation.ConversationQueryRepository;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ConversationService {

    private final ConversationQueryRepository conversationQueryRepository;
    private final ConversationRepository conversationRepository;

    public CursorResponse<ConversationModel> getAll(UUID userId, ConversationQueryRequest request) {
        return conversationQueryRepository.findAllConversation(userId, request);
    }

    public ConversationModel getById(UUID conversationId) {
        return conversationRepository.findById(conversationId)
            .orElseThrow(() -> ConversationNotFoundException.withId(conversationId));
    }

    public ConversationModel getByIdWithAccessCheck(UUID conversationId, UUID requesterId) {
        validateAccess(conversationId, requesterId);
        return getById(conversationId);
    }

    public ConversationModel create(
        ConversationModel conversationModel,
        UserModel userModel,
        UserModel withUserModel
    ) {
        return conversationRepository.save(conversationModel);
    }

    public Optional<ConversationModel> findByParticipants(UUID userId, UUID withUserId) {
        return conversationRepository.findByParticipants(userId, withUserId);
    }

    public void validateAccess(UUID conversationId, UUID requesterId) {
        // if (!conversationQueryRepository.existsParticipant(conversationId, requesterId)) {
        //     throw ConversationAccessDeniedException.withConversationIdAndUserId(
        //         conversationId,
        //         requesterId
        //     );
        // }
    }
}
