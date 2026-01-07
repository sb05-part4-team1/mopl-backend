package com.mopl.domain.service.conversation;

import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConversationService {

//    생각해봐야 할 부분
//    @Query("SELECT c FROM ConversationEntity c " +
//            "LEFT JOIN FETCH c.withUser " +
//            "LEFT JOIN FETCH c.lastMessage m " +
//            "LEFT JOIN FETCH m.sender " +
//            "WHERE c.id = :id")
//    Optional<ConversationEntity> findByIdWithDetails(@Param("id") UUID id);
//    이런 식으로 JPQL을 짜면 다른 Repository가 필요 없어지긴 합니다.
//    데이터 조합의 책임을 Service로 두면 저 쿼리를
//    Service에서 따로따로 쿼리를 날려서할 수는 있는데 복잡할 것 같네요.

    private final ConversationRepository conversationRepository;
    private final ReadStatusRepository readStatusRepository;
    private DirectMessageRepository directMessageRepository;
    // 필요할 것이라 예상

    //    private UserRepository userRepository;

    public ConversationModel create(
        ConversationModel conversationModel,
        UserModel userModel
    ) {
        ConversationModel model = conversationRepository.save(conversationModel);

        // ReadStatus 생성 후 저장 , 위는 상대/아래는 본인
        ReadStatusModel withReadStatusModel = readStatusRepository.save(ReadStatusModel.create(
            model, model.getWithUser()));
        ReadStatusModel userReadStatusModel = readStatusRepository.save(ReadStatusModel.create(
            model, userModel));

        // directMessageModel이 들어가야 됨.
        // message에 따라 hasunread 값도 같이.

        return model;

    }

    public void directMessageRead(
            DirectMessageModel directMessageModel,
            List<ReadStatusModel> readStatusModels
    ) {
        for(ReadStatusModel readStatusModel : readStatusModels){
            if( !(readStatusModel.getUser().getId().equals(directMessageModel.getSender().getId())) ){
                    
            }

        }






    }


    public ConversationModel getConversation(UUID conversationId) {

        return conversationRepository.get(conversationId);
    }


    public DirectMessageModel getDircetMassegeById(UUID directMessageId) {

        return directMessageRepository.findById(directMessageId);
    }

    public List<ReadStatusModel> getReadStatusByConversationId(UUID conversationId) {
        return readStatusRepository.findByConversationId(conversationId);
    }

    public ReadStatusModel getReadStatusById(UUID readStatusId) {
        return readStatusRepository.findById(readStatusId);
    }

    public ReadStatusModel getReadStatusByConversationIdAndParticipantId(
            UUID conversationId,
            UUID participantId
    ) {
        return readStatusRepository
                .findByConversationIdAndParticipantId(conversationId, participantId);
    }

    public DirectMessageModel getLastDirectMessage(UUID conversationId,UUID senderId) {

        return directMessageRepository.findByConversationIdAndSenderId(conversationId, senderId);
    }
}
