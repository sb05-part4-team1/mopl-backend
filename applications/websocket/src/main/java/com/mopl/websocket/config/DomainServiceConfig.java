package com.mopl.websocket.config;

import com.mopl.domain.repository.content.ContentQueryRepository;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.conversation.ConversationQueryRepository;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.domain.repository.conversation.DirectMessageQueryRepository;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import com.mopl.domain.repository.user.UserQueryRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.conversation.ConversationService;
import com.mopl.domain.service.user.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public ContentService contentService(
        ContentQueryRepository contentQueryRepository,
        ContentRepository contentRepository
    ) {
        return new ContentService(contentQueryRepository, contentRepository);
    }

    @Bean
    public ConversationService conversationService(
        ConversationQueryRepository conversationQueryRepository,
        ConversationRepository conversationRepository,
        DirectMessageQueryRepository directMessageQueryRepository,
        DirectMessageRepository directMessageRepository,
        ReadStatusRepository readStatusRepository,
        UserRepository userRepository
    ) {
        return new ConversationService(
            conversationQueryRepository,
            conversationRepository,
            directMessageQueryRepository,
            directMessageRepository,
            readStatusRepository,
            userRepository
        );
    }

    @Bean
    public UserService userService(
        UserQueryRepository userQueryRepository,
        UserRepository userRepository
    ) {
        return new UserService(userQueryRepository, userRepository);
    }
}
