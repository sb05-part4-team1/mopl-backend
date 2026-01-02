package com.mopl.jpa.repository.conversation;

import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDirectMessageRepository extends JpaRepository<DirectMessageEntity, UUID> {

}
