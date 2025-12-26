package com.mopl.jpa.repository.conversation;

import com.mopl.jpa.entity.conversation.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaConversationRepository extends JpaRepository<ConversationEntity, UUID> {

}
