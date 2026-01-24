package com.mopl.jpa.repository.conversation;

import com.mopl.jpa.entity.conversation.ReadStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            SELECT rs
            FROM ReadStatusEntity rs
            JOIN FETCH rs.participant
            WHERE rs.conversation.id IN :conversationIds
              AND rs.participant.id <> :userId
        """)
    List<ReadStatusEntity> findOthersByConversationIds(
        UUID userId,
        Collection<UUID> conversationIds
    );

    @Query("""
            SELECT rs
            FROM ReadStatusEntity rs
            JOIN FETCH rs.participant
            WHERE rs.conversation.id IN :conversationIds
              AND rs.participant.id = :userId
        """)
    List<ReadStatusEntity> findMineByConversationIds(
        @Param("conversationIds") List<UUID> conversationIds,
        @Param("userId") UUID userId
    );

}
