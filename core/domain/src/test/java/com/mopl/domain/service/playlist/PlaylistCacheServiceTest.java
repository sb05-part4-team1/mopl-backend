package com.mopl.domain.service.playlist;

import com.mopl.domain.exception.playlist.PlaylistNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaylistCacheService 단위 테스트")
class PlaylistCacheServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private PlaylistContentRepository playlistContentRepository;

    @InjectMocks
    private PlaylistCacheService playlistCacheService;

    private UserModel createOwner() {
        return UserModel.builder()
            .id(UUID.randomUUID())
            .email("owner@example.com")
            .name("소유자")
            .build();
    }

    private PlaylistModel createPlaylist(UserModel owner) {
        return PlaylistModel.create(owner, "테스트 플레이리스트", "테스트 설명");
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTest {

        @Test
        @DisplayName("존재하는 플레이리스트 ID로 조회하면 PlaylistModel 반환")
        void withExistingPlaylistId_returnsPlaylistModel() {
            // given
            UUID playlistId = UUID.randomUUID();
            UserModel owner = createOwner();
            PlaylistModel playlistModel = createPlaylist(owner);

            given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlistModel));

            // when
            PlaylistModel result = playlistCacheService.getById(playlistId);

            // then
            assertThat(result).isEqualTo(playlistModel);
            then(playlistRepository).should().findById(playlistId);
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트 ID로 조회하면 PlaylistNotFoundException 발생")
        void withNonExistingPlaylistId_throwsPlaylistNotFoundException() {
            // given
            UUID playlistId = UUID.randomUUID();

            given(playlistRepository.findById(playlistId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> playlistCacheService.getById(playlistId))
                .isInstanceOf(PlaylistNotFoundException.class);

            then(playlistRepository).should().findById(playlistId);
        }
    }

    @Nested
    @DisplayName("getContents()")
    class GetContentsTest {

        @Test
        @DisplayName("플레이리스트의 컨텐츠 목록 반환")
        void returnsContentList() {
            // given
            UUID playlistId = UUID.randomUUID();
            List<ContentModel> contents = List.of(
                ContentModel.builder().id(UUID.randomUUID()).title("컨텐츠1").build(),
                ContentModel.builder().id(UUID.randomUUID()).title("컨텐츠2").build()
            );

            given(playlistContentRepository.findContentsByPlaylistId(playlistId))
                .willReturn(contents);

            // when
            List<ContentModel> result = playlistCacheService.getContents(playlistId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(contents);
            then(playlistContentRepository).should().findContentsByPlaylistId(playlistId);
        }

        @Test
        @DisplayName("컨텐츠가 없으면 빈 목록 반환")
        void withNoContents_returnsEmptyList() {
            // given
            UUID playlistId = UUID.randomUUID();

            given(playlistContentRepository.findContentsByPlaylistId(playlistId))
                .willReturn(List.of());

            // when
            List<ContentModel> result = playlistCacheService.getContents(playlistId);

            // then
            assertThat(result).isEmpty();
            then(playlistContentRepository).should().findContentsByPlaylistId(playlistId);
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("플레이리스트 저장 성공")
        void savesPlaylist() {
            // given
            UserModel owner = createOwner();
            PlaylistModel playlistModel = createPlaylist(owner);

            given(playlistRepository.save(playlistModel)).willReturn(playlistModel);

            // when
            PlaylistModel result = playlistCacheService.save(playlistModel);

            // then
            assertThat(result).isEqualTo(playlistModel);
            then(playlistRepository).should().save(playlistModel);
        }
    }

    @Nested
    @DisplayName("evictPlaylist()")
    class EvictPlaylistTest {

        @Test
        @DisplayName("캐시 evict 메서드 호출 시 예외 없이 완료")
        void evictsWithoutException() {
            // given
            UUID playlistId = UUID.randomUUID();

            // when & then
            playlistCacheService.evictPlaylist(playlistId);
        }
    }

    @Nested
    @DisplayName("evictContents()")
    class EvictContentsTest {

        @Test
        @DisplayName("컨텐츠 캐시 evict 메서드 호출 시 예외 없이 완료")
        void evictsWithoutException() {
            // given
            UUID playlistId = UUID.randomUUID();

            // when & then
            playlistCacheService.evictContents(playlistId);
        }
    }
}
