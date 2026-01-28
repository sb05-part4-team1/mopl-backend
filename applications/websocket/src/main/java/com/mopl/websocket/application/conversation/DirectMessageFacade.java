package com.mopl.websocket.application.conversation;

import com.mopl.domain.event.conversation.DirectMessageSentEvent;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.conversation.ConversationService;
import com.mopl.domain.service.conversation.DirectMessageService;
import com.mopl.domain.service.conversation.ReadStatusService;
import com.mopl.domain.service.outbox.OutboxService;
import com.mopl.dto.conversation.DirectMessageResponse;
import com.mopl.dto.conversation.DirectMessageResponseMapper;
import com.mopl.dto.outbox.DomainEventOutboxMapper;
import com.mopl.logging.context.LogContext;
import com.mopl.redis.pubsub.DirectMessagePublisher;
import com.mopl.websocket.interfaces.api.conversation.dto.DirectMessageSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DirectMessageFacade {

    private final ConversationService conversationService;
    private final DirectMessageService directMessageService;
    private final ReadStatusService readStatusService;
    private final DirectMessageResponseMapper directMessageResponseMapper;
    private final DirectMessagePublisher directMessagePublisher;
    private final OutboxService outboxService;
    private final DomainEventOutboxMapper domainEventOutboxMapper;
    private final TransactionTemplate transactionTemplate;

    public DirectMessageResponse sendDirectMessage(UUID senderId, UUID conversationId, DirectMessageSendRequest request) {
        ReadStatusModel senderReadStatus = readStatusService.getReadStatusWithParticipant(senderId, conversationId);
        ConversationModel conversation = conversationService.getById(conversationId);
        String content = request.content();

        ReadStatusModel otherReadStatus = readStatusService.getOtherReadStatusWithParticipant(senderId, conversation.getId());
        UserModel sender = senderReadStatus.getParticipant();
        UserModel receiver = otherReadStatus != null ? otherReadStatus.getParticipant() : null;

        DirectMessageModel directMessage = DirectMessageModel.create(content, sender, conversation);

        DirectMessageModel savedDirectMessage = Objects.requireNonNull(transactionTemplate.execute(status -> {
            DirectMessageModel saved = directMessageService.save(directMessage);

            if (receiver != null) {
                DirectMessageSentEvent event = DirectMessageSentEvent.builder()
                    .messageId(saved.getId())
                    .conversationId(conversation.getId())
                    .senderId(sender.getId())
                    .senderName(sender.getName())
                    .receiverId(receiver.getId())
                    .messageContent(content)
                    .build();
                outboxService.save(domainEventOutboxMapper.toOutboxModel(event));
            }

            return saved;
        }));

        DirectMessageResponse response = directMessageResponseMapper.toResponse(savedDirectMessage, receiver);
        directMessagePublisher.publish(response);

        LogContext.with("senderId", senderId)
            .and("conversationId", conversationId)
            .and("messageId", savedDirectMessage.getId())
            .info("Direct message sent");

        return response;
    }
}
