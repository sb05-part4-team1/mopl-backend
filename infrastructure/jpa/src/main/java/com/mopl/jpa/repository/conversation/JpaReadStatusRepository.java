package com.mopl.jpa.repository.conversation;

import com.mopl.jpa.entity.conversation.ReadStatusEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface JpaReadStatusRepository extends JpaRepository<ReadStatusEntity, UUID> {

    @Query("""
               SELECT rs
               FROM ReadStatusEntity rs
               JOIN FETCH rs.participant
               WHERE rs.conversation.id = :conversationId
                 AND rs.participant.id = :participantId
        """)
    ReadStatusEntity findByConversationIdAndParticipantId(UUID conversationId, UUID participantId);

    @Query("""
             SELECT rs
             FROM ReadStatusEntity rs
             JOIN FETCH rs.participant
             WHERE rs.conversation.id = :conversationId
               AND rs.participant.id <> :userId
        """)
    ReadStatusEntity findOtherReadStatus(UUID conversationId, UUID userId);

    @EntityGraph(attributePaths = {"participant"})
    List<ReadStatusEntity> findWithParticipantByParticipantIdNotAndConversationIdIn(
        UUID participantId,
        Collection<UUID> conversationId
    );

    List<ReadStatusEntity> findByParticipantIdAndConversationIdIn(
        UUID participantId,
        Collection<UUID> conversationId
    );
}
