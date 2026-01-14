package com.mopl.jpa.repository.conversation;

import com.mopl.jpa.entity.conversation.ReadStatusEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReadStatusRepository extends JpaRepository<ReadStatusEntity, UUID> {

    List<ReadStatusEntity> findByConversationId(UUID conversationId);

    ReadStatusEntity findByConversationIdAndParticipantId(UUID conversationId, UUID participantId);

    List<ReadStatusEntity> findByParticipantId(UUID participantId);

}
