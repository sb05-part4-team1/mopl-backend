package com.mopl.domain.service.playlist;

import com.mopl.domain.exception.playlist.PlaylistContentAlreadyExistsException;
import com.mopl.domain.exception.playlist.PlaylistContentNotFoundException;
import com.mopl.domain.exception.playlist.PlaylistForbiddenException;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@DisplayName("PlaylistService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private PlaylistCacheService playlistCacheService;

    @Mock
    private PlaylistQueryRepository playlistQueryRepository;

    @Mock
    private PlaylistContentRepository playlistContentRepository;

    @InjectMocks
    private PlaylistService playlistService;

    @DisplayName("getAll()")
    @Nested
    class GetAllTest {

        @DisplayName("정상 조회 시 CursorResponse 반환")
        @Test
        void withValidRequest_returnsCursorResponse() {
            // given
            PlaylistModel playlist = PlaylistModelFixture.create();
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null,
                null,
                null,
                null,
                null,
                10,
                SortDirection.ASCENDING,
                null
            );
            CursorResponse<PlaylistModel> expectedResponse = CursorResponse.of(
                List.of(playlist),
                "nextCursor",
                UUID.randomUUID(),
                true,
                1L,
                "updatedAt",
                SortDirection.ASCENDING
            );

            given(playlistQueryRepository.findAll(request)).willReturn(expectedResponse);

            // when
            CursorResponse<PlaylistModel> result = playlistService.getAll(request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.data()).hasSize(1);
            then(playlistQueryRepository).should().findAll(request);
        }

        @DisplayName("결과가 없으면 빈 CursorResponse 반환")
        @Test
        void withNoResults_returnsEmptyCursorResponse() {
            // given
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null,
                null,
                null,
                null,
                null,
                10,
                SortDirection.ASCENDING,
                null
            );
            CursorResponse<PlaylistModel> emptyResponse = CursorResponse.empty(
                "updatedAt",
                SortDirection.ASCENDING
            );

            given(playlistQueryRepository.findAll(request)).willReturn(emptyResponse);

            // when
            CursorResponse<PlaylistModel> result = playlistService.getAll(request);

            // then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalCount()).isZero();
        }
    }

    @DisplayName("getById()")
    @Nested
    class GetByIdTest {

        @DisplayName("존재하는 플레이리스트 ID로 조회하면 PlaylistModel 반환")
        @Test
        void withExistingPlaylistId_returnsPlaylistModel() {
            // given
            UUID playlistId = UUID.randomUUID();
            PlaylistModel playlistModel = PlaylistModelFixture.create();

            given(playlistCacheService.getById(playlistId)).willReturn(playlistModel);

            // when
            PlaylistModel result = playlistService.getById(playlistId);

            // then
            assertThat(result).isEqualTo(playlistModel);
            then(playlistCacheService).should().getById(playlistId);
        }

        @DisplayName("존재하지 않는 플레이리스트 ID로 조회하면 PlaylistNotFoundException 발생")
        @Test
        void withNonExistingPlaylistId_throwsPlaylistNotFoundException() {
            // given
            UUID playlistId = UUID.randomUUID();

            given(playlistCacheService.getById(playlistId))
                .willThrow(PlaylistNotFoundException.withId(playlistId));

            // when & then
            assertThatThrownBy(() -> playlistService.getById(playlistId))
                .isInstanceOf(PlaylistNotFoundException.class);

            then(playlistCacheService).should().getById(playlistId);
        }
    }

    @DisplayName("getContentsByPlaylistId()")
    @Nested
    class GetContentsByPlaylistIdTest {

        @DisplayName("플레이리스트의 콘텐츠 목록 조회")
        @Test
        void withExistingPlaylistId_returnsContents() {
            // given
            UUID playlistId = UUID.randomUUID();
            ContentModel content = ContentModelFixture.create();
            List<ContentModel> contents = List.of(content);

            given(playlistCacheService.getContentsByPlaylistId(playlistId)).willReturn(contents);

            // when
            List<ContentModel> result = playlistService.getContentsByPlaylistId(playlistId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(content);
            then(playlistCacheService).should().getContentsByPlaylistId(playlistId);
        }

        @DisplayName("콘텐츠가 없는 플레이리스트 조회 시 빈 리스트 반환")
        @Test
        void withNoContents_returnsEmptyList() {
            // given
            UUID playlistId = UUID.randomUUID();

            given(playlistCacheService.getContentsByPlaylistId(playlistId)).willReturn(List.of());

            // when
            List<ContentModel> result = playlistService.getContentsByPlaylistId(playlistId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @DisplayName("getContentsByPlaylistIds()")
    @Nested
    class GetContentsByPlaylistIdsTest {

        @DisplayName("여러 플레이리스트의 콘텐츠 조회")
        @Test
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

        @DisplayName("빈 ID 목록으로 조회 시 빈 맵 반환")
        @Test
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

    @DisplayName("create()")
    @Nested
    class CreateTest {

        @DisplayName("유효한 플레이리스트 생성")
        @Test
        void withValidPlaylist_createsPlaylist() {
            // given
            UserModel owner = UserModelFixture.create();
            String title = "내 플레이리스트";
            String description = "플레이리스트 설명";

            given(playlistCacheService.save(any(PlaylistModel.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            PlaylistModel result = playlistService.create(title, description, owner);

            // then
            assertThat(result.getOwner()).isEqualTo(owner);
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getDescription()).isEqualTo(description);
            then(playlistCacheService).should().save(any(PlaylistModel.class));
        }
    }

    @DisplayName("update()")
    @Nested
    class UpdateTest {

        @DisplayName("소유자가 플레이리스트 수정 성공")
        @Test
        void withOwner_updatesPlaylist() {
            // given
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlistModel = PlaylistModelFixture.create(owner);
            UUID playlistId = UUID.randomUUID();
            UUID requesterId = owner.getId();
            String newTitle = "수정된 제목";
            String newDescription = "수정된 설명";

            given(playlistCacheService.getById(playlistId)).willReturn(playlistModel);
            given(playlistCacheService.save(any(PlaylistModel.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            PlaylistModel result = playlistService.update(
                playlistId, requesterId, newTitle, newDescription
            );

            // then
            assertThat(result.getTitle()).isEqualTo(newTitle);
            assertThat(result.getDescription()).isEqualTo(newDescription);
            then(playlistCacheService).should().getById(playlistId);
            then(playlistCacheService).should().save(any(PlaylistModel.class));
        }

        @DisplayName("소유자가 아닌 사용자가 수정 시 PlaylistForbiddenException 발생")
        @Test
        void withNonOwner_throwsPlaylistForbiddenException() {
            // given
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlistModel = PlaylistModelFixture.create(owner);
            UUID playlistId = UUID.randomUUID();
            UUID requesterId = UUID.randomUUID();

            given(playlistCacheService.getById(playlistId)).willReturn(playlistModel);

            // when & then
            assertThatThrownBy(
                () -> playlistService.update(playlistId, requesterId, "새 제목", "새 설명")
            ).isInstanceOf(PlaylistForbiddenException.class);

            then(playlistCacheService).should().getById(playlistId);
            then(playlistCacheService).should(never()).save(any(PlaylistModel.class));
        }
    }

    @DisplayName("delete()")
    @Nested
    class DeleteTest {

        @DisplayName("소유자가 플레이리스트 삭제 성공")
        @Test
        void withOwner_deletesPlaylist() {
            // given
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlistModel = PlaylistModelFixture.create(owner);
            UUID playlistId = UUID.randomUUID();
            UUID requesterId = owner.getId();

            given(playlistCacheService.getById(playlistId)).willReturn(playlistModel);

            // when
            playlistService.delete(playlistId, requesterId);

            // then
            assertThat(playlistModel.isDeleted()).isTrue();
            then(playlistCacheService).should().getById(playlistId);
            then(playlistCacheService).should().saveAndEvict(playlistModel);
        }

        @DisplayName("소유자가 아닌 사용자가 삭제 시 PlaylistForbiddenException 발생")
        @Test
        void withNonOwner_throwsPlaylistForbiddenException() {
            // given
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlistModel = PlaylistModelFixture.create(owner);
            UUID playlistId = UUID.randomUUID();
            UUID requesterId = UUID.randomUUID();

            given(playlistCacheService.getById(playlistId)).willReturn(playlistModel);

            // when & then
            assertThatThrownBy(() -> playlistService.delete(playlistId, requesterId))
                .isInstanceOf(PlaylistForbiddenException.class);

            then(playlistCacheService).should().getById(playlistId);
            then(playlistCacheService).should(never()).saveAndEvict(any(PlaylistModel.class));
        }
    }

    @DisplayName("addContent()")
    @Nested
    class AddContentTest {

        @DisplayName("소유자가 콘텐츠 추가 성공")
        @Test
        void withOwner_addsContent() {
            // given
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlistModel = PlaylistModelFixture.create(owner);
            UUID playlistId = UUID.randomUUID();
            UUID requesterId = owner.getId();
            UUID contentId = UUID.randomUUID();

            given(playlistCacheService.getById(playlistId)).willReturn(playlistModel);
            given(playlistContentRepository.exists(playlistId, contentId)).willReturn(false);

            // when
            playlistService.addContent(playlistId, requesterId, contentId);

            // then
            then(playlistCacheService).should().getById(playlistId);
            then(playlistContentRepository).should().exists(playlistId, contentId);
            then(playlistContentRepository).should().save(playlistId, contentId);
            then(playlistCacheService).should().evictContents(playlistId);
        }

        @DisplayName("소유자가 아닌 사용자가 콘텐츠 추가 시 PlaylistForbiddenException 발생")
        @Test
        void withNonOwner_throwsPlaylistForbiddenException() {
            // given
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlistModel = PlaylistModelFixture.create(owner);
            UUID playlistId = UUID.randomUUID();
            UUID requesterId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            given(playlistCacheService.getById(playlistId)).willReturn(playlistModel);

            // when & then
            assertThatThrownBy(
                () -> playlistService.addContent(playlistId, requesterId, contentId)
            ).isInstanceOf(PlaylistForbiddenException.class);

            then(playlistContentRepository).should(never()).save(any(UUID.class), any(UUID.class));
            then(playlistCacheService).should(never()).evictContents(any(UUID.class));
        }

        @DisplayName("이미 존재하는 콘텐츠 추가 시 PlaylistContentAlreadyExistsException 발생")
        @Test
        void withExistingContent_throwsPlaylistContentAlreadyExistsException() {
            // given
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlistModel = PlaylistModelFixture.create(owner);
            UUID playlistId = UUID.randomUUID();
            UUID requesterId = owner.getId();
            UUID contentId = UUID.randomUUID();

            given(playlistCacheService.getById(playlistId)).willReturn(playlistModel);
            given(playlistContentRepository.exists(playlistId, contentId)).willReturn(true);

            // when & then
            assertThatThrownBy(
                () -> playlistService.addContent(playlistId, requesterId, contentId)
            ).isInstanceOf(PlaylistContentAlreadyExistsException.class);

            then(playlistContentRepository).should(never()).save(any(UUID.class), any(UUID.class));
            then(playlistCacheService).should(never()).evictContents(any(UUID.class));
        }
    }

    @DisplayName("removeContent()")
    @Nested
    class RemoveContentTest {

        @DisplayName("소유자가 콘텐츠 삭제 성공")
        @Test
        void withOwner_removesContent() {
            // given
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlistModel = PlaylistModelFixture.create(owner);
            UUID playlistId = UUID.randomUUID();
            UUID requesterId = owner.getId();
            UUID contentId = UUID.randomUUID();

            given(playlistCacheService.getById(playlistId)).willReturn(playlistModel);
            given(playlistContentRepository.delete(playlistId, contentId)).willReturn(true);

            // when
            playlistService.removeContent(playlistId, requesterId, contentId);

            // then
            then(playlistCacheService).should().getById(playlistId);
            then(playlistContentRepository).should().delete(playlistId, contentId);
            then(playlistCacheService).should().evictContents(playlistId);
        }

        @DisplayName("소유자가 아닌 사용자가 콘텐츠 삭제 시 PlaylistForbiddenException 발생")
        @Test
        void withNonOwner_throwsPlaylistForbiddenException() {
            // given
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlistModel = PlaylistModelFixture.create(owner);
            UUID playlistId = UUID.randomUUID();
            UUID requesterId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            given(playlistCacheService.getById(playlistId)).willReturn(playlistModel);

            // when & then
            assertThatThrownBy(
                () -> playlistService.removeContent(playlistId, requesterId, contentId)
            ).isInstanceOf(PlaylistForbiddenException.class);

            then(playlistContentRepository).should(never()).delete(any(UUID.class), any(UUID.class));
            then(playlistCacheService).should(never()).evictContents(any(UUID.class));
        }

        @DisplayName("존재하지 않는 콘텐츠 삭제 시 PlaylistContentNotFoundException 발생")
        @Test
        void withNonExistingContent_throwsPlaylistContentNotFoundException() {
            // given
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlistModel = PlaylistModelFixture.create(owner);
            UUID playlistId = UUID.randomUUID();
            UUID requesterId = owner.getId();
            UUID contentId = UUID.randomUUID();

            given(playlistCacheService.getById(playlistId)).willReturn(playlistModel);
            given(playlistContentRepository.delete(playlistId, contentId)).willReturn(false);

            // when & then
            assertThatThrownBy(
                () -> playlistService.removeContent(playlistId, requesterId, contentId)
            ).isInstanceOf(PlaylistContentNotFoundException.class);

            then(playlistCacheService).should(never()).evictContents(any(UUID.class));
        }
    }
}
