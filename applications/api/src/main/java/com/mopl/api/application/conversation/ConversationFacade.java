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
import org.springframework.transaction.support.TransactionTemplate;

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
    private final TransactionTemplate transactionTemplate;

    @Transactional(readOnly = true)
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

        Map<UUID, ReadStatusModel> requesterReadStatusMap = readStatusService.getReadStatusMap(userId, conversationIds);
        Map<UUID, ReadStatusModel> otherReadStatusMap = readStatusService.getOtherReadStatusMapWithParticipant(userId, conversationIds);
        Map<UUID, DirectMessageModel> lastMessageMap = directMessageService.getLastDirectMessageMapWithSender(conversationIds);

        return response.map(conversation -> {
            UUID conversationId = conversation.getId();

            DirectMessageModel lastMessage = lastMessageMap.get(conversationId);
            ReadStatusModel otherReadStatus = otherReadStatusMap.get(conversationId);
            ReadStatusModel requesterReadStatus = requesterReadStatusMap.get(conversationId);

            UserModel withUser = otherReadStatus != null ? otherReadStatus.getParticipant() : null;
            boolean hasUnread = calculateHasUnread(userId, lastMessage, requesterReadStatus);

            return conversationResponseMapper.toResponse(
                conversation,
                withUser,
                lastMessage,
                hasUnread
            );
        });
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(UUID userId, UUID conversationId) {
        UserModel requester = userService.getById(userId);
        UUID requesterId = requester.getId();
        readStatusService.validateParticipant(requesterId, conversationId);

        ConversationModel conversation = conversationService.getById(conversationId);

        ReadStatusModel requesterReadStatus = readStatusService.getReadStatus(requesterId, conversationId);
        ReadStatusModel otherReadStatus = readStatusService.getOtherReadStatusWithParticipant(requesterId, conversationId);
        DirectMessageModel lastMessage = directMessageService.getLastDirectMessage(conversationId);

        UserModel withUser = otherReadStatus != null ? otherReadStatus.getParticipant() : null;
        boolean hasUnread = calculateHasUnread(requesterId, lastMessage, requesterReadStatus);

        return conversationResponseMapper.toResponse(conversation, withUser, lastMessage, hasUnread);
    }

    public ConversationResponse getConversationWith(UUID requesterId, UUID withUserId) {
        UserModel withUser = userService.getById(withUserId);

        ConversationModel conversation = conversationService.getByParticipants(requesterId, withUserId);

        ReadStatusModel requesterReadStatus = readStatusService.getReadStatus(requesterId, conversation.getId());
        DirectMessageModel lastMessage = directMessageService.getLastDirectMessage(conversation.getId());

        boolean hasUnread = calculateHasUnread(requesterId, lastMessage, requesterReadStatus);

        return conversationResponseMapper.toResponse(conversation, withUser, lastMessage, hasUnread);
    }

    public ConversationResponse createConversation(UUID requesterId, ConversationCreateRequest request) {
        UserModel requester = userService.getById(requesterId);
        UserModel withUser = userService.getById(request.withUserId());

        ConversationModel savedConversation = transactionTemplate.execute(status -> {
            ConversationModel conversation = ConversationModel.create();
            ConversationModel created = conversationService.create(conversation);

            readStatusService.create(ReadStatusModel.create(requester, created));
            readStatusService.create(ReadStatusModel.create(withUser, created));

            return created;
        });

        return conversationResponseMapper.toResponse(savedConversation, withUser);
    }

    public CursorResponse<DirectMessageResponse> getDirectMessages(
        UUID userId,
        UUID conversationId,
        DirectMessageQueryRequest request
    ) {
        UserModel requester = userService.getById(userId);
        UUID requesterId = requester.getId();
        readStatusService.validateParticipant(requesterId, conversationId);

        ReadStatusModel otherReadStatus = readStatusService.getOtherReadStatusWithParticipant(requesterId, conversationId);
        UserModel otherParticipant = otherReadStatus != null ? otherReadStatus.getParticipant() : null;

        CursorResponse<DirectMessageModel> directMessages = directMessageService.getAll(requesterId, conversationId, request);
        return directMessages.map(directMessage -> {
            UserModel receiver = directMessage.getSender().getId().equals(requesterId)
                ? otherParticipant
                : requester;
            return directMessageResponseMapper.toResponse(directMessage, receiver);
        });
    }

    public void markAsRead(UUID requesterId, UUID conversationId) {
        readStatusService.validateParticipant(requesterId, conversationId);

        DirectMessageModel lastMessage = directMessageService.getLastDirectMessage(conversationId);
        if (lastMessage == null || lastMessage.getSender().getId().equals(requesterId)) {
            return;
        }

        ReadStatusModel readStatus = readStatusService.getReadStatus(requesterId, conversationId);
        ReadStatusModel updatedReadStatus = readStatus.updateLastReadAt(lastMessage.getCreatedAt());
        if (updatedReadStatus.getLastReadAt() != readStatus.getLastReadAt()) {
            readStatusService.update(updatedReadStatus);
        }
    }

    private boolean calculateHasUnread(
        UUID requesterId,
        DirectMessageModel lastMessage,
        ReadStatusModel requesterReadStatus
    ) {
        return lastMessage != null
            && requesterReadStatus != null
            && !lastMessage.getSender().getId().equals(requesterId)
            && lastMessage.getCreatedAt().isAfter(requesterReadStatus.getLastReadAt());
    }
}
