package com.mopl.jpa.repository.conversation;

import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaDirectMessageRepository extends JpaRepository<DirectMessageEntity, UUID> {

    Optional<DirectMessageEntity> findTopByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    DirectMessageEntity findTopByConversationIdAndSenderIdOrderByCreatedAtDesc(
        UUID conversationId,
        UUID senderId
    );

    @Query("""
        SELECT dm
        FROM DirectMessageEntity dm
        WHERE dm.conversation.id = :conversationId
          AND dm.sender.id <> :userId
          AND dm.id = :directMessageId
        """)
    Optional<DirectMessageEntity> findOther(
        @Param("conversationId") UUID conversationId,
        @Param("directMessageId") UUID directMessageId,
        @Param("userId") UUID userId
    );
}
