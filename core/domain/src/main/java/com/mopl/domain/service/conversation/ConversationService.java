package com.mopl.domain.service.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.repository.conversation.ConversationRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConversationService {

    // 필요할 것이라 예상
    private final ConversationRepository conversationRepository;
//    private DirectMessageRepository directMessageRepository;
//    private UserRepository userRepository;

    public ConversationModel create(ConversationModel conversationModel) {

        return conversationRepository.save(conversationModel);
    }

}
