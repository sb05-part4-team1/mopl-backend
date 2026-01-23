package com.mopl.websocket.application.conversation;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.api.interfaces.api.conversation.mapper.DirectMessageMapper;
import com.mopl.api.interfaces.api.conversation.dto.DirectMessageResponse;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.domain.service.conversation.ConversationService;
import com.mopl.domain.service.user.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DirectMessageFacade {

    private final UserService userService;
    private final ConversationService conversationService;
    private final DirectMessageRepository directMessageRepository;
    private final DirectMessageMapper directMessageMapper;

    @Transactional
    public DirectMessageResponse sendDirectMessage(UUID conversationId, UUID userId,
        String message) {

        UserModel sender = userService.getById(userId);
        ConversationModel conversation = conversationService.getConversation(conversationId,
            userId);
        UserModel receiver = conversation.getWithUser();

        DirectMessageModel directMessageModel = DirectMessageModel.builder()
            .conversation(conversation)
            .sender(sender)
            .receiver(receiver)
            .content(message)
            .build();

        DirectMessageModel savedMessage = directMessageRepository.save(directMessageModel);

        return directMessageMapper.toResponse(savedMessage);
    }
}
