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
        CursorResponse<ConversationModel> response = conversationService.getAll(userId, request);
        List<ConversationModel> conversations = response.data();

        if (conversations.isEmpty()) {
            return CursorResponse.empty(response.sortBy(), response.sortDirection());
        }

        List<UUID> conversationIds = conversations.stream()
            .map(ConversationModel::getId)
            .toList();

        Map<UUID, DirectMessageModel> lastMessageMap = directMessageService.getLastMessagesWithSenderByConversationIdIn(conversationIds);
        Map<UUID, ReadStatusModel> otherReadStatusMap = readStatusService.getOtherReadStatusWithParticipantByConversationIdIn(userId, conversationIds);
        Map<UUID, ReadStatusModel> myReadStatusMap = readStatusService.getMyReadStatusWithParticipantByConversationIdIn(userId, conversationIds);

        return response.map(conversation -> {
            UUID conversationId = conversation.getId();

            ReadStatusModel otherReadStatus = otherReadStatusMap.get(conversationId);
            DirectMessageModel lastMessage = lastMessageMap.get(conversationId);
            ReadStatusModel myReadStatus = myReadStatusMap.get(conversationId);

            UserModel withUser = otherReadStatus != null ? otherReadStatus.getParticipant() : null;
            boolean hasUnread = calculateHasUnread(userId, lastMessage, myReadStatus);

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
        ReadStatusModel otherReadStatus = readStatusService.getOtherReadStatus(conversationId, requesterId);
        UserModel receiver = otherReadStatus != null ? otherReadStatus.getParticipant() : null;

        return directMessageService.getAllDirectMessages(conversationId, request, requesterId)
            .map(dm -> directMessageResponseMapper.toResponse(dm, receiver));
    }

    public ConversationResponse getConversation(UUID requesterId, UUID conversationId) {
        ConversationModel conversation = conversationService.getByIdWithAccessCheck(conversationId, requesterId);

        ReadStatusModel otherReadStatus = readStatusService.getOtherReadStatus(conversationId, requesterId);
        DirectMessageModel lastMessage = directMessageService.getLastMessageByConversationId(conversationId);
        ReadStatusModel myReadStatus = readStatusService.getMyReadStatus(conversationId, requesterId);

        UserModel withUser = otherReadStatus != null ? otherReadStatus.getParticipant() : null;
        boolean hasUnread = calculateHasUnread(requesterId, lastMessage, myReadStatus);

        return conversationResponseMapper.toResponse(conversation, withUser, lastMessage, hasUnread);
    }

    public ConversationResponse getConversationByWith(UUID requesterId, UUID withUserId) {
        UserModel withUser = userService.getById(withUserId);

        ConversationModel conversation = conversationService.findByParticipants(requesterId, withUserId)
            .orElseGet(() -> {
                UserModel requester = userService.getById(requesterId);
                return conversationService.create(ConversationModel.create(), requester, withUser);
            });

        DirectMessageModel lastMessage = directMessageService.getLastMessageByConversationId(conversation.getId());
        ReadStatusModel myReadStatus = readStatusService.getMyReadStatus(conversation.getId(), requesterId);

        boolean hasUnread = calculateHasUnread(requesterId, lastMessage, myReadStatus);

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

        DirectMessageModel directMessage = directMessageService.getOtherDirectMessage(
            requesterId, conversationId, directMessageId
        );

        ReadStatusModel myReadStatus = readStatusService.getMyReadStatus(conversationId, requesterId);

        readStatusService.markAsRead(directMessage, myReadStatus);
    }

    private boolean calculateHasUnread(
        UUID requesterId,
        DirectMessageModel lastMessage,
        ReadStatusModel myReadStatus
    ) {
        return lastMessage != null
            && !lastMessage.getSender().getId().equals(requesterId)
            && lastMessage.getCreatedAt().isAfter(myReadStatus.getLastReadAt());
    }
}
