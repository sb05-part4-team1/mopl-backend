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

    public CursorResponse<DirectMessageModel> getAll(
        UUID requesterId,
        UUID conversationId,
        DirectMessageQueryRequest request
    ) {
        return directMessageQueryRepository.findAll(requesterId, conversationId, request);
    }

    public DirectMessageModel getOtherDirectMessage(
        UUID conversationId,
        UUID directMessageId,
        UUID userId
    ) {
        return directMessageRepository.findOtherDirectMessage(conversationId, directMessageId, userId).orElse(null);
    }

    public Map<UUID, DirectMessageModel> getLastMessagesWithSenderByConversationIdIn(Collection<UUID> conversationIds) {
        return directMessageQueryRepository.findLastMessagesWithSenderByConversationIdIn(conversationIds);
    }

    public DirectMessageModel getLastMessageByConversationId(UUID conversationId) {
        return directMessageRepository.findLastMessageByConversationId(conversationId).orElse(null);
    }
}
