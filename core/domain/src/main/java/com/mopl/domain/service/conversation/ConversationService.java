package com.mopl.domain.service.conversation;

import com.mopl.domain.exception.conversation.ConversationNotFoundException;
import com.mopl.domain.exception.conversation.DirectMessageNotFoundException;
import com.mopl.domain.exception.conversation.ReadStatusNotFoundException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.content.ContentQueryRequest;
import com.mopl.domain.repository.conversation.ConversationQueryRepository;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.domain.repository.conversation.DirectMessageQueryRepository;
import com.mopl.domain.repository.conversation.DirectMessageQueryRequest;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ReadStatusRepository readStatusRepository;
    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;
    private final ConversationQueryRepository conversationQueryRepository;
    private final DirectMessageQueryRepository directMessageQueryRepository;

    public ConversationModel create(
        ConversationModel conversationModel,
        UserModel userModel
    ) {
        ConversationModel model = conversationRepository.save(conversationModel);
        model.withUser(userModel);

        // ReadStatus 생성 후 저장 , 위는 상대/아래는 본인
        ReadStatusModel withReadStatusModel = readStatusRepository.save(ReadStatusModel.create(
            model, model.getWithUser()));
        ReadStatusModel userReadStatusModel = readStatusRepository.save(ReadStatusModel.create(
            model, userModel));

        // directMessageModel이 들어가야 됨.
        // message에 따라 hasunread 값도 같이.
        // -> 처음 생성했을 때는 directMessage가 없어도 될 것 같음.

        return model;

    }

    public CursorResponse<DirectMessageModel> getAllDirectMessage(
            UUID conversationId,
            DirectMessageQueryRequest request,
            UUID userId
    ) {

        return directMessageQueryRepository.findAllByConversationId(conversationId,request,userId);

    }


    public CursorResponse<ConversationModel> getAllConversation(
            ConversationQueryRequest request,
            UUID userId
    ) {

        return conversationQueryRepository.findAllConversation(request,userId);
    }


    public void directMessageRead(
        DirectMessageModel directMessageModel,
        ReadStatusModel readStatusModel
    ) {
        if(directMessageModel!=null){
            readStatusModel.updateLastRead(Instant.now());
            readStatusRepository.save(readStatusModel);
        }

    }

    public DirectMessageModel getOtherDirectMessage(
            UUID conversationId,
            UUID directMessageId,
            UUID userId
    ){

        return directMessageRepository.findOtherDirectMessage(conversationId,directMessageId,userId)
                .orElseThrow(() -> new DirectMessageNotFoundException(conversationId,directMessageId,userId));
    }


    public ConversationModel getConversationByWith(UUID userId, UUID withId) {
        //readStatus 를 가지고 와서 conversation_id로 비교해서 찾은 뒤에 conversation 조회 및 message조회

        UserModel withModel = userRepository.findById(withId)
            .orElseThrow(() -> UserNotFoundException.withId(withId));

        ConversationModel conversationModel = conversationRepository.findByParticipants(userId,withId)
                .orElseThrow(()-> new  ConversationNotFoundException(userId,withId));

        conversationModel.withUser(withModel);

        return conversationModel;
    }

    public ConversationModel getConversation(UUID conversationId, UUID userId) {
        List<ReadStatusModel> userReadStatus = readStatusRepository.findByConversationId(
            conversationId);

        UserModel userModel = userReadStatus.stream()
                .filter(rs -> !rs.getUser().getId().equals((userId)))
                .map(rs -> userRepository.findById(rs.getUser().getId()).orElse(null))
                .findFirst()
                .orElse(null);


        ConversationModel conversationModel = conversationRepository.find(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        conversationModel.withUser(userModel);

        return conversationModel;
    }

    public DirectMessageModel getDirectMessageById(UUID directMessageId) {

        return directMessageRepository.findById(directMessageId)
            .orElseThrow(() -> new DirectMessageNotFoundException(directMessageId));
    }

    public List<ReadStatusModel> getReadStatusByConversationId(UUID conversationId) {
        return readStatusRepository.findByConversationId(conversationId);
    }

    public ReadStatusModel getReadStatusByConversationIdAndUserId(UUID conversationId,UUID userId) {
        return readStatusRepository.findByConversationIdAndUserId(conversationId,userId);
    }


    public ReadStatusModel getReadStatusById(UUID readStatusId) {
        return readStatusRepository.findById(readStatusId)
            .orElseThrow(() -> new ReadStatusNotFoundException(readStatusId));
    }

    public ReadStatusModel getReadStatusByConversationIdAndParticipantId(
        UUID conversationId,
        UUID participantId
    ) {
        return readStatusRepository
            .findByConversationIdAndParticipantId(conversationId, participantId);
    }

    public DirectMessageModel getLastDirectMessage(UUID conversationId, UUID senderId) {

        return directMessageRepository.findByConversationIdAndSenderId(conversationId, senderId);
    }
}
