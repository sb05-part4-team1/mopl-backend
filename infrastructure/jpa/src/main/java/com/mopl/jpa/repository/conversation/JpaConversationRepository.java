package com.mopl.jpa.repository.conversation;

import com.mopl.jpa.entity.conversation.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JpaConversationRepository extends JpaRepository<ConversationEntity, UUID> {

    @Query("""
            SELECT rs.conversation
            FROM ReadStatusEntity rs
            WHERE rs.participant.id in (:userId, :withId)
            GROUP BY rs.conversation.id
            HAVING COUNT(DISTINCT rs.participant.id) = 2
        """)
    Optional<ConversationEntity> findByParticipants(UUID userId, UUID withId);
}
