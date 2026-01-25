package com.mopl.domain.service.conversation;

import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.repository.conversation.DirectMessageQueryRepository;
import com.mopl.domain.repository.conversation.DirectMessageQueryRequest;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class DirectMessageService {

    private final DirectMessageQueryRepository directMessageQueryRepository;
    private final DirectMessageRepository directMessageRepository;

    public CursorResponse<DirectMessageModel> getDirectMessages(
        UUID conversationId,
        DirectMessageQueryRequest request
    ) {
        return directMessageQueryRepository.findAll(conversationId, request);
    }

    public Map<UUID, DirectMessageModel> getLastDirectMessageMapWithSender(Collection<UUID> conversationIds) {
        return directMessageQueryRepository.findLastDirectMessagesWithSenderByConversationIdIn(conversationIds);
    }

    public DirectMessageModel getLastDirectMessage(UUID conversationId) {
        return directMessageRepository.findLastMessageByConversationId(conversationId).orElse(null);
    }

    public DirectMessageModel getLastDirectMessageWithSender(UUID conversationId) {
        return directMessageRepository.findLastMessageWithSenderByConversationId(conversationId).orElse(null);
    }
}
