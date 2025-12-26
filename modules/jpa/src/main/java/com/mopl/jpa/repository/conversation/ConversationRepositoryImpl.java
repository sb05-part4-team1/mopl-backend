package com.mopl.jpa.repository.conversation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConversationRepositoryImpl {

    private final JpaConversationRepository jpaConversationRepository;
}
