package com.mopl.websocket.config;

import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.content.query.ContentQueryRepository;
import com.mopl.domain.repository.conversation.ConversationQueryRepository;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.domain.repository.conversation.DirectMessageQueryRepository;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import com.mopl.domain.repository.outbox.OutboxRepository;
import com.mopl.domain.repository.tag.TagRepository;
import com.mopl.domain.repository.user.UserQueryRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.content.ContentTagService;
import com.mopl.domain.service.conversation.ConversationService;
import com.mopl.domain.service.conversation.DirectMessageService;
import com.mopl.domain.service.conversation.ReadStatusService;
import com.mopl.domain.service.outbox.OutboxService;
import com.mopl.domain.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("DomainServiceConfig 단위 테스트")
class DomainServiceConfigTest {

    private final DomainServiceConfig config = new DomainServiceConfig();

    @Test
    @DisplayName("contentService - ContentService 빈 생성")
    void contentService_createsBean() {
        // given
        ContentQueryRepository contentQueryRepository = mock(ContentQueryRepository.class);
        ContentRepository contentRepository = mock(ContentRepository.class);

        // when
        ContentService service = config.contentService(contentQueryRepository, contentRepository);

        // then
        assertThat(service).isNotNull();
        assertThat(service).isInstanceOf(ContentService.class);
    }

    @Test
    @DisplayName("contentTagService - ContentTagService 빈 생성")
    void contentTagService_createsBean() {
        // given
        ContentTagRepository contentTagRepository = mock(ContentTagRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);

        // when
        ContentTagService service = config.contentTagService(contentTagRepository, tagRepository);

        // then
        assertThat(service).isNotNull();
        assertThat(service).isInstanceOf(ContentTagService.class);
    }

    @Test
    @DisplayName("conversationService - ConversationService 빈 생성")
    void conversationService_createsBean() {
        // given
        ConversationQueryRepository conversationQueryRepository = mock(ConversationQueryRepository.class);
        ConversationRepository conversationRepository = mock(ConversationRepository.class);

        // when
        ConversationService service = config.conversationService(
            conversationQueryRepository,
            conversationRepository
        );

        // then
        assertThat(service).isNotNull();
        assertThat(service).isInstanceOf(ConversationService.class);
    }

    @Test
    @DisplayName("directMessageService - DirectMessageService 빈 생성")
    void directMessageService_createsBean() {
        // given
        DirectMessageQueryRepository directMessageQueryRepository = mock(DirectMessageQueryRepository.class);
        DirectMessageRepository directMessageRepository = mock(DirectMessageRepository.class);

        // when
        DirectMessageService service = config.directMessageService(
            directMessageQueryRepository,
            directMessageRepository
        );

        // then
        assertThat(service).isNotNull();
        assertThat(service).isInstanceOf(DirectMessageService.class);
    }

    @Test
    @DisplayName("readStatusService - ReadStatusService 빈 생성")
    void readStatusService_createsBean() {
        // given
        ReadStatusRepository readStatusRepository = mock(ReadStatusRepository.class);

        // when
        ReadStatusService service = config.readStatusService(readStatusRepository);

        // then
        assertThat(service).isNotNull();
        assertThat(service).isInstanceOf(ReadStatusService.class);
    }

    @Test
    @DisplayName("userService - UserService 빈 생성")
    void userService_createsBean() {
        // given
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        // when
        UserService service = config.userService(userQueryRepository, userRepository);

        // then
        assertThat(service).isNotNull();
        assertThat(service).isInstanceOf(UserService.class);
    }

    @Test
    @DisplayName("outboxService - OutboxService 빈 생성")
    void outboxService_createsBean() {
        // given
        OutboxRepository outboxRepository = mock(OutboxRepository.class);

        // when
        OutboxService service = config.outboxService(outboxRepository);

        // then
        assertThat(service).isNotNull();
        assertThat(service).isInstanceOf(OutboxService.class);
    }
}
