package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import com.mopl.jpa.entity.conversation.DirectMessageEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DirectMessageRepositoryImpl implements DirectMessageRepository {

    private final JpaDirectMessageRepository jpaDirectMessageRepository;
    private final DirectMessageEntityMapper directMessageEntityMapper;

    @Override
    public DirectMessageModel save(DirectMessageModel directMessageModel) {
        DirectMessageEntity directMessageEntity = directMessageEntityMapper.toEntity(
                directMessageModel);
        DirectMessageEntity savedDirectMessageEntity = jpaDirectMessageRepository.save(
                directMessageEntity);
        return directMessageEntityMapper.toModel(savedDirectMessageEntity);
    }

}
