package com.mopl.api.application.conversation;

import com.mopl.api.interfaces.api.conversation.ConversationCreateRequest;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.conversation.ConversationService;
import com.mopl.domain.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ConversationFacade {
    //controller에서 받은 데이터를 service로 전달만 시키는 곳, 비즈니스 로직은 작성하지 않는다.

    private final ConversationService conversationService;
    private final UserService userService;

    @Transactional
    public ConversationModel createConversation(ConversationCreateRequest request) {

        UserModel author = userService.getById(request.withUserId());
        ConversationModel conversationModel = ConversationModel.create(request.withUserId());

        return conversationService.create(conversationModel);
    }

//    @Transactional(readOnly = true)
//    public ConversationModel getConversation(UUID conversationId) {
//        return conversationService.getById(conversationId);
//    }

}
