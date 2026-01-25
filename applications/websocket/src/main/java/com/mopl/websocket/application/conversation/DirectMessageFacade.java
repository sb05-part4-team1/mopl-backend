package com.mopl.websocket.application.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.conversation.ConversationService;
import com.mopl.domain.service.conversation.DirectMessageService;
import com.mopl.domain.service.conversation.ReadStatusService;
import com.mopl.dto.conversation.DirectMessageResponse;
import com.mopl.dto.conversation.DirectMessageResponseMapper;
import com.mopl.redis.pubsub.DirectMessagePublisher;
import com.mopl.websocket.interfaces.api.conversation.dto.DirectMessageSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DirectMessageFacade {

    private final ConversationService conversationService;
    private final DirectMessageService directMessageService;
    private final ReadStatusService readStatusService;
    private final DirectMessageResponseMapper directMessageResponseMapper;
    private final DirectMessagePublisher directMessagePublisher;

    public DirectMessageResponse sendDirectMessage(UUID senderId, UUID conversationId, DirectMessageSendRequest request) {
        ReadStatusModel requesterReadStatus = readStatusService.getReadStatus(senderId, conversationId);
        ConversationModel conversation = conversationService.getById(conversationId);
        String content = request.content();

        ReadStatusModel otherReadStatus = readStatusService.getOtherReadStatusWithParticipant(senderId, conversation.getId());
        UserModel receiver = otherReadStatus != null ? otherReadStatus.getParticipant() : null;

        DirectMessageModel directMessage = DirectMessageModel.create(content, requesterReadStatus.getParticipant(), conversation);
        DirectMessageModel savedDirectMessage = directMessageService.save(directMessage);
        DirectMessageResponse response = directMessageResponseMapper.toResponse(savedDirectMessage, receiver);

        directMessagePublisher.publish(response);

        return response;
    }
}
