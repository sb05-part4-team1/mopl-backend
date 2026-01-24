package com.mopl.websocket.application.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.domain.service.conversation.ConversationService;
import com.mopl.domain.service.conversation.ReadStatusService;
import com.mopl.domain.service.user.UserService;
import com.mopl.websocket.interfaces.api.conversation.dto.DirectMessageResponse;
import com.mopl.websocket.interfaces.api.conversation.mapper.DirectMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DirectMessageFacade {

    private final UserService userService;
    private final ConversationService conversationService;
    private final ReadStatusService readStatusService;
    private final DirectMessageRepository directMessageRepository;
    private final DirectMessageMapper directMessageMapper;

    @Transactional
    public DirectMessageResponse sendDirectMessage(UUID conversationId, UUID senderId, String message) {
        UserModel sender = userService.getById(senderId);
        ConversationModel conversation = conversationService.getByIdWithAccessCheck(conversationId, senderId);

        ReadStatusModel otherReadStatus = readStatusService.getOtherReadStatusWithParticipant(conversationId, senderId);
        UserModel receiver = otherReadStatus.getParticipant();

        DirectMessageModel directMessageModel = DirectMessageModel.create(message, conversation, sender);

        DirectMessageModel savedMessage = directMessageRepository.save(directMessageModel);

        return directMessageMapper.toResponse(savedMessage, receiver);
    }
}
