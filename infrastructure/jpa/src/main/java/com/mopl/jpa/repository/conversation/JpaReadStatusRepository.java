package com.mopl.jpa.repository.conversation;

import com.mopl.jpa.entity.conversation.ReadStatusEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaReadStatusRepository extends JpaRepository<ReadStatusEntity, UUID> {

    List<ReadStatusEntity> findByConversationId(UUID conversationId);

    //fetchjoin
    @Query("""
               SELECT rs
               FROM ReadStatusEntity rs
               JOIN FETCH rs.participant
               WHERE rs.conversation.id = :conversationId
                 AND rs.participant.id = :participantId
        """)
    ReadStatusEntity findByConversationIdAndParticipantId(UUID conversationId, UUID participantId);

    List<ReadStatusEntity> findByParticipantId(UUID participantId);

    //fetchjoin
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
        @Param("conversationIds") List<UUID> conversationIds,
        @Param("userId") UUID userId
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
