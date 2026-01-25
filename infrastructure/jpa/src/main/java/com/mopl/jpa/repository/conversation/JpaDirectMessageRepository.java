package com.mopl.jpa.repository.conversation;

import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaDirectMessageRepository extends JpaRepository<DirectMessageEntity, UUID> {

    Optional<DirectMessageEntity> findTopByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    @EntityGraph(attributePaths = {"sender"})
    Optional<DirectMessageEntity> findWithSenderTopByConversationIdOrderByCreatedAtDesc(UUID conversationId);
}
