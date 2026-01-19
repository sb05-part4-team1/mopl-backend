package com.mopl.api.application.conversation;

import com.mopl.api.interfaces.api.conversation.ConversationCreateRequest;
import com.mopl.api.interfaces.api.conversation.ConversationResponse;
import com.mopl.api.interfaces.api.conversation.ConversationResponseMapper;
import com.mopl.api.interfaces.api.conversation.DirectMessageMapper;
import com.mopl.api.interfaces.api.conversation.DirectMessageResponse;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.repository.conversation.DirectMessageQueryRequest;
import com.mopl.domain.service.conversation.ConversationService;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.support.cursor.CursorResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ConversationFacade {
    private final ConversationService conversationService;
    private final UserService userService;
    private final ConversationResponseMapper conversationResponseMapper;
    private final DirectMessageMapper directMessageMapper;

    @Transactional
    public void directMessageRead(UUID conversationId, UUID directMessageId, UUID userId) {
        DirectMessageModel directMessageModel = conversationService.getOtherDirectMessage(
            conversationId, directMessageId, userId);

        ReadStatusModel readStatusModels = conversationService
            .getReadStatusByConversationIdAndUserId(
                conversationId, userId);

        conversationService.directMessageRead(directMessageModel, readStatusModels);
    }


    public ConversationModel getConversationByWith(UUID userId, UUID withId) {

        return conversationService.getConversationByWith(userId, withId);

    }


    public CursorResponse<DirectMessageResponse> getAllDirectMessage(
        UUID conversationId,
        DirectMessageQueryRequest request,
        UUID userId
    ) {
        return conversationService.getAllDirectMessage(conversationId, request, userId)
            .map(directMessageMapper::toResponse);

    }


    public CursorResponse<ConversationResponse> getAllConversation(
        ConversationQueryRequest request,
        UUID userId
    ) {
        return conversationService.getAllConversation(request, userId)
            .map(conversationResponseMapper::toResponse);

    }

    @Transactional
    public ConversationModel createConversation(ConversationCreateRequest request, UUID userId) {

        UserModel withUserModel = userService.getById(request.withUserId());
        UserModel requesterUserModel = userService.getById(userId);
        ConversationModel conversationModel = ConversationModel.create();

        return conversationService.create(conversationModel, requesterUserModel, withUserModel);
    }


    public ConversationModel getConversation(UUID conversationId, UUID userId) {

        return conversationService.getConversation(conversationId, userId);
    }

}
