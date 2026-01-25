package com.mopl.jpa.entity.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("DirectMessageEntityMapper 단위 테스트")
class DirectMessageEntityMapperTest {

    @Mock
    private ConversationEntityMapper conversationMapper;

    @Mock
    private UserEntityMapper userMapper;

    @InjectMocks
    private DirectMessageEntityMapper directMessageEntityMapper;

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("DirectMessageEntity가 null이면 null을 반환한다")
        void withNullEntity_returnsNull() {
            DirectMessageModel result = directMessageEntityMapper.toModel(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("유효한 Entity를 ID만 포함한 Model로 변환한다")
        void withValidEntity_mapsToModelWithIdOnly() {
            // given
            UUID messageId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            String content = "안녕하세요";
            Instant now = Instant.now();

            ConversationEntity conversationEntity = ConversationEntity.builder()
                .id(conversationId)
                .build();
            UserEntity senderEntity = UserEntity.builder()
                .id(senderId)
                .build();

            DirectMessageEntity entity = DirectMessageEntity.builder()
                .id(messageId)
                .conversation(conversationEntity)
                .sender(senderEntity)
                .content(content)
                .createdAt(now)
                .build();

            // when
            DirectMessageModel result = directMessageEntityMapper.toModel(entity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(messageId);
            assertThat(result.getContent()).isEqualTo(content);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getConversation().getId()).isEqualTo(conversationId);
            assertThat(result.getSender().getId()).isEqualTo(senderId);

            verifyNoInteractions(conversationMapper);
            verifyNoInteractions(userMapper);
        }

        @Test
        @DisplayName("Entity의 conversation이 null이면 Model의 conversation도 null이다")
        void withNullConversation_mapsNull() {
            // given
            DirectMessageEntity entity = DirectMessageEntity.builder()
                .id(UUID.randomUUID())
                .conversation(null)
                .sender(UserEntity.builder().id(UUID.randomUUID()).build())
                .content("내용")
                .build();

            // when
            DirectMessageModel result = directMessageEntityMapper.toModel(entity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getConversation()).isNull();
        }

        @Test
        @DisplayName("Entity의 sender가 null이면 Model의 sender도 null이다")
        void withNullSender_mapsNull() {
            // given
            DirectMessageEntity entity = DirectMessageEntity.builder()
                .id(UUID.randomUUID())
                .conversation(ConversationEntity.builder().id(UUID.randomUUID()).build())
                .sender(null)
                .content("내용")
                .build();

            // when
            DirectMessageModel result = directMessageEntityMapper.toModel(entity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getSender()).isNull();
        }
    }

    @Nested
    @DisplayName("toModelWithSender()")
    class ToModelWithSenderTest {

        @Test
        @DisplayName("DirectMessageEntity가 null이면 null을 반환한다")
        void withNullEntity_returnsNull() {
            DirectMessageModel result = directMessageEntityMapper.toModelWithSender(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Sender 전체 모델을 포함한 Model로 변환한다")
        void withValidEntity_mapsToModelWithFullSender() {
            // given
            UUID messageId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();
            String content = "안녕하세요";
            Instant now = Instant.now();

            ConversationEntity conversationEntity = ConversationEntity.builder()
                .id(conversationId)
                .build();
            UserEntity senderEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .build();
            UserModel expectedSender = mock(UserModel.class);

            DirectMessageEntity entity = DirectMessageEntity.builder()
                .id(messageId)
                .conversation(conversationEntity)
                .sender(senderEntity)
                .content(content)
                .createdAt(now)
                .build();

            given(userMapper.toModel(senderEntity)).willReturn(expectedSender);

            // when
            DirectMessageModel result = directMessageEntityMapper.toModelWithSender(entity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(messageId);
            assertThat(result.getContent()).isEqualTo(content);
            assertThat(result.getConversation().getId()).isEqualTo(conversationId);
            assertThat(result.getSender()).isEqualTo(expectedSender);

            verify(userMapper).toModel(senderEntity);
            verifyNoInteractions(conversationMapper);
        }

        @Test
        @DisplayName("Entity의 sender가 null이면 Model의 sender도 null이다")
        void withNullSender_mapsNull() {
            // given
            DirectMessageEntity entity = DirectMessageEntity.builder()
                .id(UUID.randomUUID())
                .conversation(ConversationEntity.builder().id(UUID.randomUUID()).build())
                .sender(null)
                .content("내용")
                .build();

            given(userMapper.toModel(null)).willReturn(null);

            // when
            DirectMessageModel result = directMessageEntityMapper.toModelWithSender(entity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getSender()).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("DirectMessageModel이 null이면 null을 반환한다")
        void withNullModel_returnsNull() {
            DirectMessageEntity result = directMessageEntityMapper.toEntity(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("유효한 Model을 Entity로 변환한다")
        void withValidModel_mapsToEntity() {
            // given
            UUID messageId = UUID.randomUUID();
            String content = "안녕하세요";
            Instant now = Instant.now();

            ConversationModel conversationModel = mock(ConversationModel.class);
            UserModel senderModel = mock(UserModel.class);
            ConversationEntity expectedConversation = ConversationEntity.builder()
                .id(UUID.randomUUID())
                .build();
            UserEntity expectedSender = UserEntity.builder()
                .id(UUID.randomUUID())
                .build();

            DirectMessageModel model = DirectMessageModel.builder()
                .id(messageId)
                .conversation(conversationModel)
                .sender(senderModel)
                .content(content)
                .createdAt(now)
                .build();

            given(conversationMapper.toEntity(conversationModel)).willReturn(expectedConversation);
            given(userMapper.toEntity(senderModel)).willReturn(expectedSender);

            // when
            DirectMessageEntity result = directMessageEntityMapper.toEntity(model);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(messageId);
            assertThat(result.getContent()).isEqualTo(content);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getConversation()).isEqualTo(expectedConversation);
            assertThat(result.getSender()).isEqualTo(expectedSender);

            verify(conversationMapper).toEntity(conversationModel);
            verify(userMapper).toEntity(senderModel);
        }

        @Test
        @DisplayName("Model의 conversation이 null이면 Entity의 conversation도 null이다")
        void withNullConversation_mapsNull() {
            // given
            DirectMessageModel model = DirectMessageModel.builder()
                .id(UUID.randomUUID())
                .conversation(null)
                .sender(mock(UserModel.class))
                .content("내용")
                .build();

            given(conversationMapper.toEntity(null)).willReturn(null);
            given(userMapper.toEntity(model.getSender())).willReturn(mock(UserEntity.class));

            // when
            DirectMessageEntity result = directMessageEntityMapper.toEntity(model);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getConversation()).isNull();
        }

        @Test
        @DisplayName("Model의 sender가 null이면 Entity의 sender도 null이다")
        void withNullSender_mapsNull() {
            // given
            DirectMessageModel model = DirectMessageModel.builder()
                .id(UUID.randomUUID())
                .conversation(mock(ConversationModel.class))
                .sender(null)
                .content("내용")
                .build();

            given(conversationMapper.toEntity(model.getConversation())).willReturn(mock(ConversationEntity.class));
            given(userMapper.toEntity(null)).willReturn(null);

            // when
            DirectMessageEntity result = directMessageEntityMapper.toEntity(model);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getSender()).isNull();
        }
    }
}
