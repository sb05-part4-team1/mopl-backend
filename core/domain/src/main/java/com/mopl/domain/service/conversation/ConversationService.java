package com.mopl.domain.service.conversation;

import com.mopl.domain.exception.conversation.ConversationNotFoundException;
import com.mopl.domain.exception.conversation.DirectMessageNotFoundException;
import com.mopl.domain.exception.conversation.ReadStatusNotFoundException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import com.mopl.domain.repository.user.UserRepository;
import java.time.Instant;
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
    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;

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

    public void directMessageRead(
        DirectMessageModel directMessageModel,
        List<ReadStatusModel> readStatusModels
    ) {
        for (ReadStatusModel readStatusModel : readStatusModels) {
            if (!(readStatusModel.getUser().getId().equals(directMessageModel.getSender()
                .getId()))) {
                readStatusModel.updateLastRead(Instant.now());
                readStatusRepository.save(readStatusModel);
            }

        }

    }

    public ConversationModel getConversationByWith(UUID userId, UUID withId) {
        //readStatus 를 가지고 와서 conversation_id로 비교해서 찾은 뒤에 conversation 조회 및 message조회

        List<ReadStatusModel> userReadStatus = readStatusRepository.findByParticipantId(userId);
        List<ReadStatusModel> withReadStatus = readStatusRepository.findByParticipantId(withId);
        UserModel withModel = userRepository.findById(withId)
            .orElseThrow(() -> UserNotFoundException.withId(withId));
        UUID conversationId = null;

        // QueryDSl로 조회하는 쿼리 작성 고려하기
        for (ReadStatusModel userRead : userReadStatus) {
            for (ReadStatusModel withRead : withReadStatus) {
                if (userRead.getConversation().getId().equals(withRead.getConversation().getId())) {
                    conversationId = userRead.getConversation().getId();
                    break;
                }
            }
            if (conversationId != null) {
                break;
            }
        }

        UUID finalConversationId = conversationId;

        ConversationModel conversationModel = conversationRepository.get(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException(finalConversationId));

        conversationModel.withUser(withModel);

        return conversationModel;
    }

    public ConversationModel getConversation(UUID conversationId, UUID userId) {
        List<ReadStatusModel> userReadStatus = readStatusRepository.findByConversationId(
            conversationId);
        UserModel userModel = null;

        for (ReadStatusModel readStatus : userReadStatus) {
            if (!(readStatus.getUser().getId().equals(userId))) {
                userModel = userRepository.findById(userId)
                    .orElseThrow(() -> UserNotFoundException.withId(userId));
            }
        }

        ConversationModel conversationModel = conversationRepository.get(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        conversationModel.withUser(userModel);

        return conversationModel;
    }

    public DirectMessageModel getDircetMassegeById(UUID directMessageId) {

        return directMessageRepository.findById(directMessageId)
            .orElseThrow(() -> new DirectMessageNotFoundException(directMessageId));
    }

    public List<ReadStatusModel> getReadStatusByConversationId(UUID conversationId) {
        return readStatusRepository.findByConversationId(conversationId);
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
