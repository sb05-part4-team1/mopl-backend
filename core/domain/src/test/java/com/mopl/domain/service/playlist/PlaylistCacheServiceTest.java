package com.mopl.domain.service.playlist;

import com.mopl.domain.exception.playlist.PlaylistNotFoundException;
import com.mopl.domain.fixture.ContentModelFixture;
import com.mopl.domain.fixture.PlaylistModelFixture;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;
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

    @Nested
    @DisplayName("getById()")
    class GetByIdTest {

        @Test
        @DisplayName("존재하는 플레이리스트 ID로 조회하면 PlaylistModel 반환")
        void withExistingPlaylistId_returnsPlaylistModel() {
            // given
            UUID playlistId = UUID.randomUUID();
            PlaylistModel playlistModel = PlaylistModelFixture.create();

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
    @DisplayName("getContentsByPlaylistId()")
    class GetContentsByPlaylistIdTest {

        @Test
        @DisplayName("플레이리스트의 컨텐츠 목록 반환")
        void returnsContentList() {
            // given
            UUID playlistId = UUID.randomUUID();
            List<ContentModel> contents = List.of(
                ContentModelFixture.create(),
                ContentModelFixture.create()
            );

            given(playlistContentRepository.findContentsByPlaylistId(playlistId))
                .willReturn(contents);

            // when
            List<ContentModel> result = playlistCacheService.getContentsByPlaylistId(playlistId);

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
            List<ContentModel> result = playlistCacheService.getContentsByPlaylistId(playlistId);

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
            PlaylistModel playlistModel = PlaylistModelFixture.create();

            given(playlistRepository.save(playlistModel)).willReturn(playlistModel);

            // when
            PlaylistModel result = playlistCacheService.save(playlistModel);

            // then
            assertThat(result).isEqualTo(playlistModel);
            then(playlistRepository).should().save(playlistModel);
        }
    }

    @Nested
    @DisplayName("saveAndEvict()")
    class SaveAndEvictTest {

        @Test
        @DisplayName("플레이리스트 저장 후 캐시 evict")
        void savesPlaylistAndEvictsCache() {
            // given
            PlaylistModel playlistModel = PlaylistModelFixture.create();

            // when
            playlistCacheService.saveAndEvict(playlistModel);

            // then
            then(playlistRepository).should().save(playlistModel);
        }
    }
}
