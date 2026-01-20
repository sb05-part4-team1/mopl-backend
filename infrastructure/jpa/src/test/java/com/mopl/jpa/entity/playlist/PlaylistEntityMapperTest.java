package com.mopl.jpa.entity.playlist;

import com.mopl.domain.model.playlist.PlaylistModel;
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
@DisplayName("PlaylistEntityMapper 단위 테스트")
class PlaylistEntityMapperTest {

    @Mock
    private UserEntityMapper userEntityMapper;

    @InjectMocks
    private PlaylistEntityMapper playlistEntityMapper;

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("PlaylistEntity가 null이면 null을 반환한다")
        void withNullEntity_returnsNull() {
            PlaylistModel result = playlistEntityMapper.toModel(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("유효한 Entity를 Owner ID만 포함한 Model로 변환한다")
        void withValidEntity_mapsToModelWithOwnerIdOnly() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            String title = "플레이리스트 제목";
            String description = "플레이리스트 설명";
            Instant now = Instant.now();

            UserEntity ownerEntity = UserEntity.builder().id(ownerId).build();

            PlaylistEntity playlistEntity = PlaylistEntity.builder()
                .id(playlistId)
                .owner(ownerEntity)
                .title(title)
                .description(description)
                .createdAt(now)
                .updatedAt(now)
                .build();

            // when
            PlaylistModel result = playlistEntityMapper.toModel(playlistEntity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(playlistId);
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getDescription()).isEqualTo(description);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
            assertThat(result.getOwner().getId()).isEqualTo(ownerId);

            verifyNoInteractions(userEntityMapper);
        }

        @Test
        @DisplayName("Entity의 owner가 null이면 Model의 owner도 null이다")
        void withNullOwner_mapsNull() {
            // given
            PlaylistEntity playlistEntity = PlaylistEntity.builder()
                .id(UUID.randomUUID())
                .owner(null)
                .title("제목")
                .build();

            // when
            PlaylistModel result = playlistEntityMapper.toModel(playlistEntity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOwner()).isNull();
        }
    }

    @Nested
    @DisplayName("toModelWithOwner()")
    class ToModelWithOwnerTest {

        @Test
        @DisplayName("PlaylistEntity가 null이면 null을 반환한다")
        void withNullEntity_returnsNull() {
            PlaylistModel result = playlistEntityMapper.toModelWithOwner(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Owner 전체 모델을 포함한 Model로 변환한다")
        void withValidEntity_mapsToModelWithFullOwner() {
            // given
            UUID playlistId = UUID.randomUUID();
            String title = "플레이리스트 제목";
            String description = "플레이리스트 설명";
            Instant now = Instant.now();

            UserEntity ownerEntity = UserEntity.builder().id(UUID.randomUUID()).build();
            UserModel expectedOwner = mock(UserModel.class);

            PlaylistEntity playlistEntity = PlaylistEntity.builder()
                .id(playlistId)
                .owner(ownerEntity)
                .title(title)
                .description(description)
                .createdAt(now)
                .updatedAt(now)
                .build();

            given(userEntityMapper.toModel(ownerEntity)).willReturn(expectedOwner);

            // when
            PlaylistModel result = playlistEntityMapper.toModelWithOwner(playlistEntity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(playlistId);
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getDescription()).isEqualTo(description);
            assertThat(result.getOwner()).isEqualTo(expectedOwner);

            verify(userEntityMapper).toModel(ownerEntity);
        }

        @Test
        @DisplayName("Entity의 owner가 null이면 Model의 owner도 null이다")
        void withNullOwner_mapsNull() {
            // given
            PlaylistEntity playlistEntity = PlaylistEntity.builder()
                .id(UUID.randomUUID())
                .owner(null)
                .title("제목")
                .build();

            given(userEntityMapper.toModel(null)).willReturn(null);

            // when
            PlaylistModel result = playlistEntityMapper.toModelWithOwner(playlistEntity);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOwner()).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("PlaylistModel이 null이면 null을 반환한다")
        void withNullModel_returnsNull() {
            PlaylistEntity result = playlistEntityMapper.toEntity(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("유효한 Model을 Entity로 변환한다")
        void withValidModel_mapsToEntity() {
            // given
            UUID playlistId = UUID.randomUUID();
            String title = "플레이리스트 제목";
            String description = "플레이리스트 설명";
            Instant now = Instant.now();

            UserModel ownerModel = mock(UserModel.class);
            UserEntity expectedOwnerEntity = UserEntity.builder().id(UUID.randomUUID()).build();

            PlaylistModel playlistModel = PlaylistModel.builder()
                .id(playlistId)
                .owner(ownerModel)
                .title(title)
                .description(description)
                .createdAt(now)
                .updatedAt(now)
                .build();

            given(userEntityMapper.toEntity(ownerModel)).willReturn(expectedOwnerEntity);

            // when
            PlaylistEntity result = playlistEntityMapper.toEntity(playlistModel);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(playlistId);
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getDescription()).isEqualTo(description);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
            assertThat(result.getOwner()).isEqualTo(expectedOwnerEntity);

            verify(userEntityMapper).toEntity(ownerModel);
        }

        @Test
        @DisplayName("Model의 owner가 null이면 Entity의 owner도 null이다")
        void withNullOwner_mapsNull() {
            // given
            PlaylistModel playlistModel = PlaylistModel.builder()
                .id(UUID.randomUUID())
                .owner(null)
                .title("제목")
                .build();

            given(userEntityMapper.toEntity(null)).willReturn(null);

            // when
            PlaylistEntity result = playlistEntityMapper.toEntity(playlistModel);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOwner()).isNull();
        }
    }
}
