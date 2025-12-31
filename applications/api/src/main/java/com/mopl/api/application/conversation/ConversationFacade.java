package com.mopl.api.application.conversation;

import com.mopl.api.interfaces.api.conversation.ConversationCreateRequest;
import com.mopl.api.interfaces.api.user.UserCreateRequest;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.user.AuthProvider;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.user.UserService;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public  class ConversationFacade {
    //controller에서 받은 데이터를 service로 전달만 시키는 곳, 비즈니스 로직은 작성하지 않는다.

//    private final ConversationService conversationService;

    @Transactional
    public ConversationModel createConversation(ConversationCreateRequest request) {

        //conversationCreateRequest에 상대방의 userId만 온다.
        //최종적으로는 conversationResponse(Dto)에 converstaionId와 상대방 정보
        //lastestMessage
        //마지막에 읽지 않은 메시지가 있는지 true/false 가 포함
        //내 정보는 security에서 가지고 오는걸로 작성
        //ConversationModel을 만들어서 service에 던지는 역할


        ConversationModel conversationModel = ConversationModel.create(request.withUserId());

//        return conversationService.create(conversationModel);
        return null;
    }

//    @Transactional(readOnly = true)
//    public ConversationModel getConversation(UUID conversationId) {
//        return conversationService.getById(conversationId);
//    }




}