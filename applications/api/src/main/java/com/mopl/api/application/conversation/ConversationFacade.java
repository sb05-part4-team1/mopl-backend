package com.mopl.api.application.conversation;

import com.mopl.api.interfaces.api.conversation.ConversationCreateRequest;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.conversation.ConversationService;
import com.mopl.domain.service.user.UserService;
import java.util.List;
import java.util.UUID;
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
    public void directMessageRead(UUID conversationId,UUID directMessageId){
        DirectMessageModel directMessageModel =
                conversationService.getDircetMassegeById(directMessageId);

        List<ReadStatusModel> readStatusModels =
                conversationService.getReadStatusByConversationId(directMessageId);

        conversationService.directMessageRead(directMessageModel,readStatusModels);


    }

    public ConversationModel getConversationByWith(UUID userId,UUID withId){

        return conversationService.getConversationByWith(userId,withId);

    }

    @Transactional
    public ConversationModel createConversation(ConversationCreateRequest request, UUID userId) {

        UserModel withModel = userService.getById(request.withUserId()); //나중에 더 이상 사용되지 않으면 변수는 없애기
        UserModel userModel = userService.getById(userId);
        ConversationModel conversationModel = ConversationModel.create(withModel);

        return conversationService.create(conversationModel, userModel);
    }

    @Transactional
    public ConversationModel getConversation(UUID conversationId,UUID userId) {

        return conversationService.getConversation(conversationId,userId);
    }

//    @Transactional(readOnly = true)
//    public ConversationModel getConversation(UUID conversationId) {
//        return conversationService.getById(conversationId);
//    }

}
