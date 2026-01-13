package com.mopl.jpa.repository.conversation;

import com.mopl.jpa.entity.conversation.ConversationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaConversationRepository extends JpaRepository<ConversationEntity, UUID> {


    @Query("""
        SELECT rs.conversation.id
        FROM ReadStatusEntity rs
        WHERE rs.participant.id in (:userId, :withId)
        GROUP By rs.conversation.id
        HAVING COUNT(DISTINCT rs.participant.id) = 2
    """)
    Optional<UUID> findConversationIdByParticipants(
            @Param("userId") UUID userId,
            @Param("withId") UUID withId
    );
}
