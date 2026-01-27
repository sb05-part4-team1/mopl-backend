package com.mopl.domain.service.playlist;

import com.mopl.domain.exception.playlist.PlaylistContentAlreadyExistsException;
import com.mopl.domain.exception.playlist.PlaylistContentNotFoundException;
import com.mopl.domain.exception.playlist.PlaylistNotFoundException;
import com.mopl.domain.fixture.ContentModelFixture;
import com.mopl.domain.fixture.PlaylistModelFixture;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.playlist.PlaylistContentRepository;
import com.mopl.domain.repository.playlist.PlaylistQueryRepository;
import com.mopl.domain.repository.playlist.PlaylistQueryRequest;
import com.mopl.domain.repository.playlist.PlaylistRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaylistService 단위 테스트")
class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private PlaylistQueryRepository playlistQueryRepository;

    @Mock
    private PlaylistContentRepository playlistContentRepository;

    @InjectMocks
    private PlaylistService playlistService;

    @Nested
    @DisplayName("getAll()")
    class GetAllTest {

        @Test
        @DisplayName("Repository에 위임하여 결과 반환")
        void delegatesToRepository() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, 10, SortDirection.ASCENDING, null
            );
            CursorResponse<PlaylistModel> expectedResponse = CursorResponse.empty(
                "UPDATED_AT", SortDirection.ASCENDING
            );

            given(playlistQueryRepository.findAll(request)).willReturn(expectedResponse);

            // when
            CursorResponse<PlaylistModel> result = playlistService.getAll(request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(playlistQueryRepository).should().findAll(request);
        }
    }

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
            PlaylistModel result = playlistService.getById(playlistId);

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
            assertThatThrownBy(() -> playlistService.getById(playlistId))
                .isInstanceOf(PlaylistNotFoundException.class)
                .satisfies(e -> {
                    PlaylistNotFoundException ex = (PlaylistNotFoundException) e;
                    assertThat(ex.getDetails().get("id")).isEqualTo(playlistId);
                });

            then(playlistRepository).should().findById(playlistId);
        }
    }

    @Nested
    @DisplayName("getContentsByPlaylistId()")
    class GetContentsByPlaylistIdTest {

        @Test
        @DisplayName("플레이리스트의 콘텐츠 목록 조회")
        void withExistingPlaylistId_returnsContents() {
            // given
            UUID playlistId = UUID.randomUUID();
            ContentModel content = ContentModelFixture.create();
            List<ContentModel> contents = List.of(content);

            given(playlistContentRepository.findContentsByPlaylistId(playlistId)).willReturn(contents);

            // when
            List<ContentModel> result = playlistService.getContentsByPlaylistId(playlistId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(content);
            then(playlistContentRepository).should().findContentsByPlaylistId(playlistId);
        }

        @Test
        @DisplayName("콘텐츠가 없는 플레이리스트 조회 시 빈 리스트 반환")
        void withNoContents_returnsEmptyList() {
            // given
            UUID playlistId = UUID.randomUUID();

            given(playlistContentRepository.findContentsByPlaylistId(playlistId)).willReturn(List.of());

            // when
            List<ContentModel> result = playlistService.getContentsByPlaylistId(playlistId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getContentsByPlaylistIdIn()")
    class GetContentsByPlaylistIdInTest {

        @Test
        @DisplayName("여러 플레이리스트의 콘텐츠 조회")
        void withMultiplePlaylistIds_returnsContentsMap() {
            // given
            UUID playlistId1 = UUID.randomUUID();
            UUID playlistId2 = UUID.randomUUID();
            List<UUID> playlistIds = List.of(playlistId1, playlistId2);

            ContentModel content1 = ContentModelFixture.create();
            ContentModel content2 = ContentModelFixture.create();
            Map<UUID, List<ContentModel>> expectedMap = Map.of(
                playlistId1, List.of(content1),
                playlistId2, List.of(content2)
            );

            given(playlistContentRepository.findContentsByPlaylistIdIn(playlistIds))
                .willReturn(expectedMap);

            // when
            Map<UUID, List<ContentModel>> result = playlistService.getContentsByPlaylistIdIn(
                playlistIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(playlistId1)).containsExactly(content1);
            assertThat(result.get(playlistId2)).containsExactly(content2);
        }

        @Test
        @DisplayName("빈 ID 목록으로 조회 시 빈 맵 반환")
        void withEmptyPlaylistIds_returnsEmptyMap() {
            // given
            List<UUID> emptyIds = List.of();

            given(playlistContentRepository.findContentsByPlaylistIdIn(emptyIds))
                .willReturn(Map.of());

            // when
            Map<UUID, List<ContentModel>> result = playlistService.getContentsByPlaylistIdIn(
                emptyIds);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 플레이리스트 생성")
        void withValidPlaylist_createsPlaylist() {
            // given
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlistModel = PlaylistModel.create(
                "내 플레이리스트",
                "플레이리스트 설명",
                owner
            );

            given(playlistRepository.save(playlistModel)).willReturn(playlistModel);

            // when
            PlaylistModel result = playlistService.create(playlistModel);

            // then
            assertThat(result).isEqualTo(playlistModel);
            then(playlistRepository).should().save(playlistModel);
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("플레이리스트 수정 성공")
        void withValidPlaylist_updatesPlaylist() {
            // given
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlistModel = PlaylistModelFixture.create(owner);
            PlaylistModel updatedPlaylist = playlistModel.update("수정된 제목", "수정된 설명");

            given(playlistRepository.save(updatedPlaylist)).willReturn(updatedPlaylist);

            // when
            PlaylistModel result = playlistService.update(updatedPlaylist);

            // then
            assertThat(result).isEqualTo(updatedPlaylist);
            then(playlistRepository).should().save(updatedPlaylist);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("플레이리스트 삭제 성공")
        void withValidPlaylist_deletesPlaylist() {
            // given
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlistModel = PlaylistModelFixture.create(owner);
            UUID playlistId = playlistModel.getId();

            // when
            playlistService.delete(playlistId);

            // then
            then(playlistRepository).should().delete(playlistId);
        }
    }

    @Nested
    @DisplayName("addContent()")
    class AddContentTest {

        @Test
        @DisplayName("콘텐츠 추가 성공")
        void withValidContent_addsContent() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            given(playlistContentRepository.exists(playlistId, contentId)).willReturn(false);

            // when
            playlistService.addContent(playlistId, contentId);

            // then
            then(playlistContentRepository).should().exists(playlistId, contentId);
            then(playlistContentRepository).should().save(playlistId, contentId);
        }

        @Test
        @DisplayName("이미 존재하는 콘텐츠 추가 시 PlaylistContentAlreadyExistsException 발생")
        void withExistingContent_throwsPlaylistContentAlreadyExistsException() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            given(playlistContentRepository.exists(playlistId, contentId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> playlistService.addContent(playlistId, contentId))
                .isInstanceOf(PlaylistContentAlreadyExistsException.class)
                .satisfies(e -> {
                    PlaylistContentAlreadyExistsException ex = (PlaylistContentAlreadyExistsException) e;
                    assertThat(ex.getDetails().get("playlistId")).isEqualTo(playlistId);
                    assertThat(ex.getDetails().get("contentId")).isEqualTo(contentId);
                });

            then(playlistContentRepository).should(never()).save(any(UUID.class), any(UUID.class));
        }
    }

    @Nested
    @DisplayName("removeContent()")
    class RemoveContentTest {

        @Test
        @DisplayName("콘텐츠 삭제 성공")
        void withExistingContent_removesContent() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            given(playlistContentRepository.deleteByPlaylistIdAndContentId(playlistId, contentId)).willReturn(true);

            // when
            playlistService.deleteContentFromPlaylist(playlistId, contentId);

            // then
            then(playlistContentRepository).should().deleteByPlaylistIdAndContentId(playlistId, contentId);
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 삭제 시 PlaylistContentNotFoundException 발생")
        void withNonExistingContent_throwsPlaylistContentNotFoundException() {
            // given
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            given(playlistContentRepository.deleteByPlaylistIdAndContentId(playlistId, contentId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> playlistService.deleteContentFromPlaylist(playlistId, contentId))
                .isInstanceOf(PlaylistContentNotFoundException.class)
                .satisfies(e -> {
                    PlaylistContentNotFoundException ex = (PlaylistContentNotFoundException) e;
                    assertThat(ex.getDetails().get("playlistId")).isEqualTo(playlistId);
                    assertThat(ex.getDetails().get("contentId")).isEqualTo(contentId);
                });
        }
    }
}
