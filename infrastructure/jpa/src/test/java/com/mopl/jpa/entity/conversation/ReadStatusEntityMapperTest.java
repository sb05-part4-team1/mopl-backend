package com.mopl.jpa.entity.conversation;

import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
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
@DisplayName("ReadStatusEntityMapper 단위 테스트")
class ReadStatusEntityMapperTest {

    @Mock
    private ConversationEntityMapper conversationMapper;

    @Mock
    private UserEntityMapper userMapper;

    @InjectMocks
    private ReadStatusEntityMapper readStatusEntityMapper;

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("ReadStatusEntity가 null이면 null을 반환한다")
        void withNullEntity_returnsNull() {
            ReadStatusModel result = readStatusEntityMapper.toModel(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("유효한 Entity를 ID만 포함한 Model로 변환한다")
        void withValidEntity_mapsToModelWithIdOnly() {
            // given
            UUID readStatusId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();
            Instant lastReadAt = now.minusSeconds(60);

            ConversationEntity conversationEntity = ConversationEntity.builder()
                .id(conversationId)
                .build();
            UserEntity userEntity = UserEntity.builder()
                .id(userId)
                .build();

            ReadStatusEntity entity = ReadStatusEntity.builder()
                .id(readStatusId)
                .conversation(conversationEntity)
                .participant(userEntity)
                .lastReadAt(lastReadAt)
                .createdAt(now)
                .build();

            // when
            ReadStatusModel result = readStatusEntityMapper.toModel(entity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(readStatusId);
            assertThat(result.getLastReadAt()).isEqualTo(lastReadAt);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getConversation().getId()).isEqualTo(conversationId);
            assertThat(result.getParticipant().getId()).isEqualTo(userId);

            verifyNoInteractions(conversationMapper);
            verifyNoInteractions(userMapper);
        }

        @Test
        @DisplayName("Entity의 conversation이 null이면 Model의 conversation도 null이다")
        void withNullConversation_mapsNull() {
            // given
            ReadStatusEntity entity = ReadStatusEntity.builder()
                .id(UUID.randomUUID())
                .conversation(null)
                .participant(UserEntity.builder().id(UUID.randomUUID()).build())
                .lastReadAt(Instant.now())
                .build();

            // when
            ReadStatusModel result = readStatusEntityMapper.toModel(entity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getConversation()).isNull();
        }

        @Test
        @DisplayName("Entity의 participant가 null이면 Model의 user도 null이다")
        void withNullParticipant_mapsNull() {
            // given
            ReadStatusEntity entity = ReadStatusEntity.builder()
                .id(UUID.randomUUID())
                .conversation(ConversationEntity.builder().id(UUID.randomUUID()).build())
                .participant(null)
                .lastReadAt(Instant.now())
                .build();

            // when
            ReadStatusModel result = readStatusEntityMapper.toModel(entity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getParticipant()).isNull();
        }
    }

    @Nested
    @DisplayName("toModelWithParticipant()")
    class ToModelWithParticipantTest {

        @Test
        @DisplayName("ReadStatusEntity가 null이면 null을 반환한다")
        void withNullEntity_returnsNull() {
            ReadStatusModel result = readStatusEntityMapper.toModelWithParticipant(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("User 전체 모델을 포함한 Model로 변환한다")
        void withValidEntity_mapsToModelWithFullUser() {
            // given
            UUID readStatusId = UUID.randomUUID();
            UUID conversationId = UUID.randomUUID();
            Instant now = Instant.now();
            Instant lastReadAt = now.minusSeconds(60);

            ConversationEntity conversationEntity = ConversationEntity.builder()
                .id(conversationId)
                .build();
            UserEntity userEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .build();
            UserModel expectedUser = mock(UserModel.class);

            ReadStatusEntity entity = ReadStatusEntity.builder()
                .id(readStatusId)
                .conversation(conversationEntity)
                .participant(userEntity)
                .lastReadAt(lastReadAt)
                .createdAt(now)
                .build();

            given(userMapper.toModel(userEntity)).willReturn(expectedUser);

            // when
            ReadStatusModel result = readStatusEntityMapper.toModelWithParticipant(entity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(readStatusId);
            assertThat(result.getLastReadAt()).isEqualTo(lastReadAt);
            assertThat(result.getConversation().getId()).isEqualTo(conversationId);
            assertThat(result.getParticipant()).isEqualTo(expectedUser);

            verify(userMapper).toModel(userEntity);
            verifyNoInteractions(conversationMapper);
        }

        @Test
        @DisplayName("Entity의 participant가 null이면 Model의 user도 null이다")
        void withNullParticipant_mapsNull() {
            // given
            ReadStatusEntity entity = ReadStatusEntity.builder()
                .id(UUID.randomUUID())
                .conversation(ConversationEntity.builder().id(UUID.randomUUID()).build())
                .participant(null)
                .lastReadAt(Instant.now())
                .build();

            given(userMapper.toModel(null)).willReturn(null);

            // when
            ReadStatusModel result = readStatusEntityMapper.toModelWithParticipant(entity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getParticipant()).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("ReadStatusModel이 null이면 null을 반환한다")
        void withNullModel_returnsNull() {
            ReadStatusEntity result = readStatusEntityMapper.toEntity(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("유효한 Model을 Entity로 변환한다")
        void withValidModel_mapsToEntity() {
            // given
            UUID readStatusId = UUID.randomUUID();
            Instant now = Instant.now();
            Instant lastReadAt = now.minusSeconds(60);

            ConversationModel conversationModel = mock(ConversationModel.class);
            UserModel userModel = mock(UserModel.class);
            ConversationEntity expectedConversation = ConversationEntity.builder()
                .id(UUID.randomUUID())
                .build();
            UserEntity expectedUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .build();

            ReadStatusModel model = ReadStatusModel.builder()
                .id(readStatusId)
                .conversation(conversationModel)
                .participant(userModel)
                .lastReadAt(lastReadAt)
                .createdAt(now)
                .build();

            given(conversationMapper.toEntity(conversationModel)).willReturn(expectedConversation);
            given(userMapper.toEntity(userModel)).willReturn(expectedUser);

            // when
            ReadStatusEntity result = readStatusEntityMapper.toEntity(model);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(readStatusId);
            assertThat(result.getLastReadAt()).isEqualTo(lastReadAt);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getConversation()).isEqualTo(expectedConversation);
            assertThat(result.getParticipant()).isEqualTo(expectedUser);

            verify(conversationMapper).toEntity(conversationModel);
            verify(userMapper).toEntity(userModel);
        }

        @Test
        @DisplayName("Model의 conversation이 null이면 Entity의 conversation도 null이다")
        void withNullConversation_mapsNull() {
            // given
            ReadStatusModel model = ReadStatusModel.builder()
                .id(UUID.randomUUID())
                .conversation(null)
                .participant(mock(UserModel.class))
                .lastReadAt(Instant.now())
                .build();

            given(conversationMapper.toEntity(null)).willReturn(null);
            given(userMapper.toEntity(model.getParticipant())).willReturn(mock(UserEntity.class));

            // when
            ReadStatusEntity result = readStatusEntityMapper.toEntity(model);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getConversation()).isNull();
        }

        @Test
        @DisplayName("Model의 user가 null이면 Entity의 participant도 null이다")
        void withNullUser_mapsNull() {
            // given
            ReadStatusModel model = ReadStatusModel.builder()
                .id(UUID.randomUUID())
                .conversation(mock(ConversationModel.class))
                .participant(null)
                .lastReadAt(Instant.now())
                .build();

            given(conversationMapper.toEntity(model.getConversation())).willReturn(mock(ConversationEntity.class));
            given(userMapper.toEntity(null)).willReturn(null);

            // when
            ReadStatusEntity result = readStatusEntityMapper.toEntity(model);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getParticipant()).isNull();
        }
    }
}
