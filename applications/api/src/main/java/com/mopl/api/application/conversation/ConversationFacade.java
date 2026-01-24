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
import com.mopl.domain.service.conversation.DirectMessageService;
import com.mopl.domain.service.conversation.ReadStatusService;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConversationFacade {

    private final ConversationService conversationService;
    private final DirectMessageService directMessageService;
    private final ReadStatusService readStatusService;
    private final UserService userService;
    private final ConversationResponseMapper conversationResponseMapper;
    private final DirectMessageResponseMapper directMessageResponseMapper;

    public CursorResponse<ConversationResponse> getConversations(
        UUID userId,
        ConversationQueryRequest request
    ) {
        CursorResponse<ConversationModel> response = conversationService.getAll(request);
        List<ConversationModel> conversations = response.data();

        if (conversations.isEmpty()) {
            return CursorResponse.empty(response.sortBy(), response.sortDirection());
        }

        List<UUID> conversationIds = conversations.stream()
            .map(ConversationModel::getId)
            .toList();

        Map<UUID, DirectMessageModel> lastMessageMap = directMessageService.getLastMessagesByConversationIdIn(conversationIds);
        Map<UUID, ReadStatusModel> otherReadStatusMap =
            readStatusService.getOtherReadStatusWithUserByConversationIdIn(conversationIds, userId);
        Map<UUID, ReadStatusModel> myReadStatusMap =
            readStatusService.getMyReadStatusByConversationIdIn(conversationIds, userId);

        return response.map(conversation -> {
            UUID conversationId = conversation.getId();

            ReadStatusModel otherReadStatus = otherReadStatusMap.get(conversationId);
            DirectMessageModel lastMessage = lastMessageMap.get(conversationId);
            ReadStatusModel myReadStatus = myReadStatusMap.get(conversationId);

            UserModel withUser = otherReadStatus != null ? otherReadStatus.getUser() : null;
            boolean hasUnread = calculateHasUnread(lastMessage, myReadStatus, userId);

            return conversationResponseMapper.toResponse(
                conversation,
                withUser,
                lastMessage,
                hasUnread
            );
        });
    }

    public CursorResponse<DirectMessageResponse> getDirectMessages(
        UUID requesterId,
        UUID conversationId,
        DirectMessageQueryRequest request
    ) {
        return conversationService.getAllDirectMessages(conversationId, request, requesterId)
            .map(directMessageResponseMapper::toResponse);
    }

    public ConversationResponse getConversation(UUID requesterId, UUID conversationId) {
        ConversationModel conversation = conversationService.getByIdWithAccessCheck(conversationId, requesterId);

        ReadStatusModel otherReadStatus = conversationService.getOtherReadStatus(conversationId, requesterId);
        DirectMessageModel lastMessage = conversationService.getLastMessageByConversationId(conversationId)
            .orElse(null);
        ReadStatusModel myReadStatus = conversationService.getMyReadStatus(conversationId, requesterId);

        UserModel withUser = otherReadStatus != null ? otherReadStatus.getUser() : null;
        boolean hasUnread = calculateHasUnread(lastMessage, myReadStatus, requesterId);

        return conversationResponseMapper.toResponse(conversation, withUser, lastMessage, hasUnread);
    }

    public ConversationResponse getConversationByWith(UUID requesterId, UUID withUserId) {
        UserModel withUser = userService.getById(withUserId);

        ConversationModel conversation = conversationService.findByParticipants(requesterId, withUserId)
            .orElseGet(() -> {
                UserModel requester = userService.getById(requesterId);
                return conversationService.create(ConversationModel.create(), requester, withUser);
            });

        DirectMessageModel lastMessage = conversationService.getLastMessageByConversationId(conversation.getId())
            .orElse(null);
        ReadStatusModel myReadStatus = conversationService.getMyReadStatus(conversation.getId(), requesterId);

        boolean hasUnread = calculateHasUnread(lastMessage, myReadStatus, requesterId);

        return conversationResponseMapper.toResponse(conversation, withUser, lastMessage, hasUnread);
    }

    @Transactional
    public ConversationResponse createConversation(UUID requesterId, ConversationCreateRequest request) {
        UserModel withUser = userService.getById(request.withUserId());
        UserModel requester = userService.getById(requesterId);

        ConversationModel conversation = conversationService.create(
            ConversationModel.create(),
            requester,
            withUser
        );

        return conversationResponseMapper.toResponse(conversation, withUser, null, false);
    }

    @Transactional
    public void directMessageRead(UUID requesterId, UUID conversationId, UUID directMessageId) {
        conversationService.validateAccess(conversationId, requesterId);

        DirectMessageModel directMessage = conversationService.findOtherDirectMessage(
            conversationId, directMessageId, requesterId
        ).orElse(null);

        ReadStatusModel myReadStatus = conversationService.getMyReadStatus(conversationId, requesterId);

        conversationService.markAsRead(directMessage, myReadStatus);
    }

    private boolean calculateHasUnread(
        DirectMessageModel lastMessage,
        ReadStatusModel myReadStatus,
        UUID requesterId
    ) {
        if (lastMessage == null) {
            return false;
        }
        if (lastMessage.getSender().getId().equals(requesterId)) {
            return false;
        }
        if (myReadStatus == null) {
            return true;
        }
        return lastMessage.getCreatedAt().isAfter(myReadStatus.getLastRead());
    }
}
