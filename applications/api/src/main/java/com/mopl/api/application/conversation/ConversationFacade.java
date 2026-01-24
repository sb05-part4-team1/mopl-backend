package com.mopl.api.application.conversation;

import com.mopl.api.interfaces.api.conversation.dto.ConversationCreateRequest;
import com.mopl.api.interfaces.api.conversation.dto.ConversationResponse;
import com.mopl.api.interfaces.api.conversation.dto.DirectMessageResponse;
import com.mopl.api.interfaces.api.conversation.mapper.ConversationResponseMapper;
import com.mopl.api.interfaces.api.conversation.mapper.DirectMessageResponseMapper;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.repository.conversation.DirectMessageQueryRequest;
import com.mopl.domain.service.conversation.ConversationService;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConversationFacade {

    private final ConversationService conversationService;
    private final UserService userService;
    private final ConversationResponseMapper conversationResponseMapper;
    private final DirectMessageResponseMapper directMessageResponseMapper;

    @Transactional
    public CursorResponse<ConversationResponse> getConversations(
        UUID userId,
        ConversationQueryRequest request
    ) {
        return conversationService.getAllConversation(request, userId)
            .map(conversationResponseMapper::toResponse);
    }

    @Transactional
    public CursorResponse<DirectMessageResponse> getDirectMessages(
        UUID requesterId,
        UUID conversationId,
        DirectMessageQueryRequest request
    ) {
        return conversationService.getAllDirectMessage(requesterId, conversationId, request)
            .map(directMessageResponseMapper::toResponse);
    }

    @Transactional
    public ConversationResponse getConversation(UUID requesterId, UUID conversationId) {
        ConversationModel conversationModel = conversationService.getConversation(requesterId, conversationId);
        return conversationResponseMapper.toResponse(conversationModel);
    }

    @Transactional
    public ConversationResponse getConversationByWith(UUID requesterId, UUID withId) {
        ConversationModel conversationModel = conversationService.getConversationByWith(requesterId, withId);
        return conversationResponseMapper.toResponse(conversationModel);
    }

    @Transactional
    public ConversationResponse createConversation(UUID requesterId, ConversationCreateRequest request) {

        UserModel withUserModel = userService.getById(request.withUserId());
        UserModel requesterUserModel = userService.getById(requesterId);
        ConversationModel conversationModel = ConversationModel.create();

        ConversationModel createdConversationModel =
            conversationService.create(conversationModel, requesterUserModel, withUserModel);

        return conversationResponseMapper.toResponse(createdConversationModel);
    }

    @Transactional
    public void directMessageRead(
        UUID requesterId,
        UUID conversationId,
        UUID directMessageId
    ) {
        DirectMessageModel directMessageModel = conversationService.getOtherDirectMessage(
            conversationId, directMessageId, requesterId);

        ReadStatusModel readStatusModels = conversationService
            .getReadStatusByConversationIdAndUserId(
                conversationId, requesterId);

        conversationService.directMessageRead(directMessageModel, readStatusModels);
    }
}
