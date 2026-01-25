package com.mopl.jpa.repository.conversation;

import com.mopl.jpa.entity.conversation.ReadStatusEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaReadStatusRepository extends JpaRepository<ReadStatusEntity, UUID> {

    List<ReadStatusEntity> findByParticipantIdAndConversationIdIn(UUID participantId, Collection<UUID> conversationIds);

    @EntityGraph(attributePaths = {"participant"})
    List<ReadStatusEntity> findWithParticipantByParticipantIdNotAndConversationIdIn(UUID participantId, Collection<UUID> conversationIds);

    Optional<ReadStatusEntity> findByParticipantIdAndConversationId(UUID participantId, UUID conversationId);

    @EntityGraph(attributePaths = {"participant"})
    Optional<ReadStatusEntity> findWithParticipantByParticipantIdAndConversationId(UUID participantId, UUID conversationId);

    @EntityGraph(attributePaths = {"participant"})
    Optional<ReadStatusEntity> findWithParticipantByParticipantIdNotAndConversationId(UUID participantId, UUID conversationId);
}
