package com.mopl.domain.service.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.conversation.ConversationRepository;
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

    // 필요할 것이라 예상
    private final ConversationRepository conversationRepository;
//    private DirectMessageRepository directMessageRepository;
//    private UserRepository userRepository;

    public ConversationModel create(
            ConversationModel conversationModel

    ) {
        ConversationModel model =conversationRepository.save(conversationModel);
        // directMessageModel이 들어가야 됨.
        // message에 따라 hasunread 값도 같이.


        return model;

    }

}
