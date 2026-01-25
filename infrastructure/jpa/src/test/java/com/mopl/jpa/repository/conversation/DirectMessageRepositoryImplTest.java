package com.mopl.jpa.repository.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.config.QuerydslConfig;
import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.jpa.entity.conversation.ConversationEntityMapper;
import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import com.mopl.jpa.entity.conversation.DirectMessageEntityMapper;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    QuerydslConfig.class,
    DirectMessageRepositoryImpl.class,
    DirectMessageEntityMapper.class,
    ConversationEntityMapper.class,
    UserEntityMapper.class
})
@DisplayName("DirectMessageRepositoryImpl 슬라이스 테스트")
class DirectMessageRepositoryImplTest {

    @Autowired
    private DirectMessageRepository directMessageRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID conversationId;
    private UUID emptyConversationId;
    private UUID senderId;

    @BeforeEach
    void setUp() {
        Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        UserEntity sender = UserEntity.builder()
            .createdAt(baseTime)
            .updatedAt(baseTime)
            .authProvider(UserModel.AuthProvider.EMAIL)
            .email("sender@example.com")
            .name("Sender")
            .password("encodedPassword")
            .role(UserModel.Role.USER)
            .locked(false)
            .build();
        entityManager.persist(sender);
        senderId = sender.getId();

        ConversationEntity conversation = ConversationEntity.builder()
            .createdAt(baseTime)
            .updatedAt(baseTime)
            .build();
        entityManager.persist(conversation);
        conversationId = conversation.getId();

        DirectMessageEntity message = DirectMessageEntity.builder()
            .conversation(conversation)
            .sender(sender)
            .content("테스트 메시지")
            .build();
        entityManager.persist(message);

        ConversationEntity emptyConversation = ConversationEntity.builder()
            .createdAt(baseTime)
            .updatedAt(baseTime)
            .build();
        entityManager.persist(emptyConversation);
        emptyConversationId = emptyConversation.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("findLastMessageByConversationId()")
    class FindLastMessageByConversationIdTest {

        @Test
        @DisplayName("메시지가 있는 대화의 마지막 메시지 조회")
        void withMessages_returnsLastMessage() {
            // when
            Optional<DirectMessageModel> result = directMessageRepository.findLastMessageByConversationId(conversationId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getContent()).isEqualTo("테스트 메시지");
            assertThat(result.get().getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("메시지가 없는 대화는 빈 Optional 반환")
        void withNoMessages_returnsEmpty() {
            // when
            Optional<DirectMessageModel> result = directMessageRepository.findLastMessageByConversationId(emptyConversationId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 대화 ID는 빈 Optional 반환")
        void withNonExistingConversation_returnsEmpty() {
            // when
            Optional<DirectMessageModel> result = directMessageRepository.findLastMessageByConversationId(UUID.randomUUID());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findLastMessageWithSenderByConversationId()")
    class FindLastMessageWithSenderByConversationIdTest {

        @Test
        @DisplayName("마지막 메시지와 sender 정보를 함께 조회")
        void withMessages_returnsLastMessageWithSender() {
            // when
            Optional<DirectMessageModel> result = directMessageRepository.findLastMessageWithSenderByConversationId(conversationId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getContent()).isEqualTo("테스트 메시지");
            assertThat(result.get().getSender()).isNotNull();
            assertThat(result.get().getSender().getId()).isEqualTo(senderId);
            assertThat(result.get().getSender().getName()).isEqualTo("Sender");
        }

        @Test
        @DisplayName("메시지가 없는 대화는 빈 Optional 반환")
        void withNoMessages_returnsEmpty() {
            // when
            Optional<DirectMessageModel> result = directMessageRepository.findLastMessageWithSenderByConversationId(emptyConversationId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("새 메시지 저장 성공")
        void withNewMessage_savesSuccessfully() {
            // given
            DirectMessageModel newMessage = DirectMessageModel.builder()
                .conversation(ConversationModel.builder().id(conversationId).build())
                .sender(UserModel.builder().id(senderId).build())
                .content("새로운 메시지")
                .build();

            // when
            DirectMessageModel savedMessage = directMessageRepository.save(newMessage);

            // then
            assertThat(savedMessage.getId()).isNotNull();
            assertThat(savedMessage.getContent()).isEqualTo("새로운 메시지");
            assertThat(savedMessage.getCreatedAt()).isNotNull();
            assertThat(savedMessage.getSender()).isNotNull();
            assertThat(savedMessage.getSender().getId()).isEqualTo(senderId);

            // DB에서 조회 확인
            entityManager.flush();
            entityManager.clear();
            Optional<DirectMessageModel> found = directMessageRepository.findLastMessageByConversationId(conversationId);
            assertThat(found).isPresent();
            assertThat(found.get().getContent()).isEqualTo("새로운 메시지");
        }
    }
}
