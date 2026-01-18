package com.mopl.api.application.playlist;

import com.mopl.api.interfaces.api.playlist.PlaylistCreateRequest;
import com.mopl.api.interfaces.api.playlist.PlaylistResponse;
import com.mopl.api.interfaces.api.playlist.PlaylistResponseMapper;
import com.mopl.api.interfaces.api.playlist.PlaylistUpdateRequest;
import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.fixture.ContentModelFixture;
import com.mopl.domain.fixture.PlaylistModelFixture;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.playlist.PlaylistQueryRequest;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.playlist.PlaylistService;
import com.mopl.domain.service.playlist.PlaylistSubscriptionService;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaylistFacade 단위 테스트")
class PlaylistFacadeTest {

    @Mock
    private PlaylistService playlistService;

    @Mock
    private PlaylistSubscriptionService playlistSubscriptionService;

    @Mock
    private UserService userService;

    @Mock
    private ContentService contentService;

    @Mock
    private PlaylistResponseMapper playlistResponseMapper;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private PlaylistFacade playlistFacade;

    @Nested
    @DisplayName("getPlaylists()")
    class GetPlaylistsTest {

        @Test
        @DisplayName("유효한 요청 시 플레이리스트 목록 조회 성공")
        void withValidRequest_getsPlaylistsSuccess() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID requesterId = owner.getId();
            PlaylistModel playlistModel = PlaylistModelFixture.builder(owner).sample();
            UUID playlistId = playlistModel.getId();
            List<UUID> playlistIds = List.of(playlistId);
            long subscriberCount = 10L;

            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, 10, SortDirection.ASCENDING, null
            );

            CursorResponse<PlaylistModel> playlistPage = CursorResponse.of(
                List.of(playlistModel),
                null,
                null,
                false,
                1L,
                "updatedAt",
                SortDirection.ASCENDING
            );

            PlaylistResponse expectedResponse = createPlaylistResponse(
                playlistId, subscriberCount, true
            );

            given(playlistService.getAll(request)).willReturn(playlistPage);
            given(playlistSubscriptionService.getSubscriberCounts(playlistIds))
                .willReturn(Map.of(playlistId, subscriberCount));
            given(playlistSubscriptionService.findSubscribedPlaylistIds(requesterId, playlistIds))
                .willReturn(Set.of(playlistId));
            given(playlistService.getContentsByPlaylistIds(playlistIds))
                .willReturn(Map.of(playlistId, Collections.emptyList()));
            given(playlistResponseMapper.toResponse(
                playlistModel, subscriberCount, true, Collections.emptyList()
            )).willReturn(expectedResponse);

            // when
            CursorResponse<PlaylistResponse> result = playlistFacade.getPlaylists(
                requesterId, request
            );

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().get(0))
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);

            then(playlistService).should().getAll(request);
            then(playlistSubscriptionService).should().getSubscriberCounts(playlistIds);
            then(playlistSubscriptionService).should()
                .findSubscribedPlaylistIds(requesterId, playlistIds);
            then(playlistService).should().getContentsByPlaylistIds(playlistIds);
        }

        @Test
        @DisplayName("빈 목록 시 빈 응답 반환")
        void withEmptyList_returnsEmptyResponse() {
            // given
            UUID requesterId = UUID.randomUUID();
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, 10, SortDirection.ASCENDING, null
            );

            CursorResponse<PlaylistModel> emptyPage = CursorResponse.empty(
                "updatedAt", SortDirection.ASCENDING
            );

            given(playlistService.getAll(request)).willReturn(emptyPage);

            // when
            CursorResponse<PlaylistResponse> result = playlistFacade.getPlaylists(
                requesterId, request
            );

            // then
            assertThat(result.data()).isEmpty();

            then(playlistSubscriptionService).should(never()).getSubscriberCounts(any());
            then(playlistSubscriptionService).should(never())
                .findSubscribedPlaylistIds(any(), any());
            then(playlistService).should(never()).getContentsByPlaylistIds(any());
        }

        @Test
        @DisplayName("비로그인 사용자(requesterId가 null)일 때 구독 여부는 모두 false")
        void withNullRequesterId_subscribedByMeIsFalse() {
            // given
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlistModel = PlaylistModelFixture.builder(owner).sample();
            UUID playlistId = playlistModel.getId();
            List<UUID> playlistIds = List.of(playlistId);
            long subscriberCount = 5L;

            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, 10, SortDirection.ASCENDING, null
            );

            CursorResponse<PlaylistModel> playlistPage = CursorResponse.of(
                List.of(playlistModel),
                null,
                null,
                false,
                1L,
                "updatedAt",
                SortDirection.ASCENDING
            );

            PlaylistResponse expectedResponse = createPlaylistResponse(
                playlistId, subscriberCount, false
            );

            given(playlistService.getAll(request)).willReturn(playlistPage);
            given(playlistSubscriptionService.getSubscriberCounts(playlistIds))
                .willReturn(Map.of(playlistId, subscriberCount));
            given(playlistSubscriptionService.findSubscribedPlaylistIds(null, playlistIds))
                .willReturn(Collections.emptySet());
            given(playlistService.getContentsByPlaylistIds(playlistIds))
                .willReturn(Map.of(playlistId, Collections.emptyList()));
            given(playlistResponseMapper.toResponse(
                playlistModel, subscriberCount, false, Collections.emptyList()
            )).willReturn(expectedResponse);

            // when
            CursorResponse<PlaylistResponse> result = playlistFacade.getPlaylists(null, request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().get(0))
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);

            then(playlistSubscriptionService).should()
                .findSubscribedPlaylistIds(null, playlistIds);
        }
    }

    @Nested
    @DisplayName("getPlaylist()")
    class GetPlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 플레이리스트 상세 정보 조회 성공")
        void withValidRequest_getsPlaylistSuccess() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID playlistId = UUID.randomUUID();
            PlaylistModel playlistModel = PlaylistModelFixture.builder(owner)
                .set("id", playlistId)
                .sample();
            List<ContentModel> contents = List.of(ContentModelFixture.create());
            long subscriberCount = 10L;
            boolean subscribedByMe = true;

            PlaylistResponse expectedResponse = createPlaylistResponse(
                playlistId, playlistModel.getTitle(), playlistModel.getDescription(),
                subscriberCount, subscribedByMe
            );

            given(userService.getById(owner.getId())).willReturn(owner);
            given(playlistService.getById(playlistId)).willReturn(playlistModel);
            given(playlistSubscriptionService.getSubscriberCount(playlistId))
                .willReturn(subscriberCount);
            given(playlistSubscriptionService.isSubscribedByPlaylistIdAndSubscriberId(
                playlistId, owner.getId()
            )).willReturn(subscribedByMe);
            given(playlistService.getContentsByPlaylistId(playlistId)).willReturn(contents);
            given(playlistResponseMapper.toResponse(
                playlistModel, subscriberCount, subscribedByMe, contents
            )).willReturn(expectedResponse);

            // when
            PlaylistResponse result = playlistFacade.getPlaylist(owner.getId(), playlistId);

            // then
            assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);

            then(userService).should().getById(owner.getId());
            then(playlistService).should().getById(playlistId);
            then(playlistSubscriptionService).should().getSubscriberCount(playlistId);
            then(playlistSubscriptionService).should()
                .isSubscribedByPlaylistIdAndSubscriberId(playlistId, owner.getId());
            then(playlistService).should().getContentsByPlaylistId(playlistId);
            then(playlistResponseMapper).should()
                .toResponse(playlistModel, subscriberCount, subscribedByMe, contents);
        }
    }

    @Nested
    @DisplayName("createPlaylist()")
    class CreatePlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 플레이리스트 생성 성공")
        void withValidRequest_createsPlaylistSuccess() {
            // given
            UserModel owner = UserModelFixture.create();
            String title = "내 플레이리스트";
            String description = "플레이리스트 설명";
            PlaylistCreateRequest request = new PlaylistCreateRequest(title, description);

            PlaylistModel playlistModel = PlaylistModelFixture.builder(owner)
                .set("title", title)
                .set("description", description)
                .sample();

            PlaylistResponse expectedResponse = createPlaylistResponse(
                playlistModel.getId(), title, description, 0L, false
            );

            given(userService.getById(owner.getId())).willReturn(owner);
            given(playlistService.create(eq(owner), eq(title), eq(description)))
                .willReturn(playlistModel);
            given(playlistResponseMapper.toResponse(playlistModel)).willReturn(expectedResponse);

            // when
            PlaylistResponse result = playlistFacade.createPlaylist(owner.getId(), request);

            // then
            assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);

            then(userService).should().getById(owner.getId());
            then(playlistService).should().create(eq(owner), eq(title), eq(description));
            then(playlistResponseMapper).should().toResponse(playlistModel);
        }
    }

    @Nested
    @DisplayName("updatePlaylist()")
    class UpdatePlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 플레이리스트 수정 성공")
        void withValidRequest_updatesPlaylistSuccess() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID playlistId = UUID.randomUUID();
            String newTitle = "수정된 제목";
            String newDescription = "수정된 설명";
            PlaylistUpdateRequest request = new PlaylistUpdateRequest(newTitle, newDescription);

            PlaylistModel updatedPlaylist = PlaylistModelFixture.builder(owner)
                .set("id", playlistId)
                .set("title", newTitle)
                .set("description", newDescription)
                .sample();

            PlaylistResponse expectedResponse = createPlaylistResponse(
                playlistId, newTitle, newDescription, 0L, false
            );

            given(userService.getById(owner.getId())).willReturn(owner);
            given(playlistService.update(playlistId, owner.getId(), newTitle, newDescription))
                .willReturn(updatedPlaylist);
            given(playlistResponseMapper.toResponse(updatedPlaylist)).willReturn(expectedResponse);

            // when
            PlaylistResponse result = playlistFacade.updatePlaylist(
                owner.getId(), playlistId, request
            );

            // then
            assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);

            then(userService).should().getById(owner.getId());
            then(playlistService).should()
                .update(playlistId, owner.getId(), newTitle, newDescription);
            then(playlistResponseMapper).should().toResponse(updatedPlaylist);
        }
    }

    @Nested
    @DisplayName("deletePlaylist()")
    class DeletePlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 플레이리스트 삭제 성공")
        void withValidRequest_deletesPlaylistSuccess() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID playlistId = UUID.randomUUID();

            given(userService.getById(owner.getId())).willReturn(owner);
            willDoNothing().given(playlistService).delete(playlistId, owner.getId());

            // when & then
            assertThatNoException()
                .isThrownBy(() -> playlistFacade.deletePlaylist(owner.getId(), playlistId));

            then(userService).should().getById(owner.getId());
            then(playlistService).should().delete(playlistId, owner.getId());
        }
    }

    @Nested
    @DisplayName("addContentToPlaylist()")
    class AddContentToPlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 콘텐츠 추가 성공")
        void withValidRequest_addsContentSuccess() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            given(userService.getById(owner.getId())).willReturn(owner);
            given(contentService.exists(contentId)).willReturn(true);
            willDoNothing().given(playlistService).addContent(playlistId, owner.getId(), contentId);

            // when & then
            assertThatNoException()
                .isThrownBy(() -> playlistFacade.addContentToPlaylist(owner.getId(), playlistId,
                    contentId));

            then(userService).should().getById(owner.getId());
            then(contentService).should().exists(contentId);
            then(playlistService).should().addContent(playlistId, owner.getId(), contentId);
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 추가 시 ContentNotFoundException 발생")
        void withNonExistingContent_throwsContentNotFoundException() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            given(userService.getById(owner.getId())).willReturn(owner);
            given(contentService.exists(contentId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> playlistFacade.addContentToPlaylist(owner.getId(), playlistId,
                contentId))
                .isInstanceOf(ContentNotFoundException.class);

            then(playlistService).should(never()).addContent(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("deleteContentFromPlaylist()")
    class DeleteContentFromPlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 콘텐츠 삭제 성공")
        void withValidRequest_deletesContentSuccess() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID playlistId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            given(userService.getById(owner.getId())).willReturn(owner);
            willDoNothing().given(playlistService).removeContent(playlistId, owner.getId(),
                contentId);

            // when & then
            assertThatNoException()
                .isThrownBy(() -> playlistFacade.deleteContentFromPlaylist(owner.getId(),
                    playlistId, contentId));

            then(userService).should().getById(owner.getId());
            then(playlistService).should().removeContent(playlistId, owner.getId(), contentId);
        }
    }

    @Nested
    @DisplayName("subscribePlaylist()")
    class SubscribePlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 구독 성공")
        @SuppressWarnings("unchecked")
        void withValidRequest_subscribesSuccess() {
            // given
            UserModel user = UserModelFixture.create();
            UUID playlistId = UUID.randomUUID();
            PlaylistModel playlistModel = PlaylistModelFixture.create();

            given(userService.getById(user.getId())).willReturn(user);
            given(playlistService.getById(playlistId)).willReturn(playlistModel);
            willAnswer(invocation -> {
                invocation.getArgument(0, Consumer.class).accept(null);
                return null;
            }).given(transactionTemplate).executeWithoutResult(any());
            willDoNothing().given(playlistSubscriptionService).subscribe(playlistId, user.getId());

            // when & then
            assertThatNoException()
                .isThrownBy(() -> playlistFacade.subscribePlaylist(user.getId(), playlistId));

            then(userService).should().getById(user.getId());
            then(playlistService).should().getById(playlistId);
            then(playlistSubscriptionService).should().subscribe(playlistId, user.getId());
        }
    }

    @Nested
    @DisplayName("unsubscribePlaylist()")
    class UnsubscribePlaylistTest {

        @Test
        @DisplayName("유효한 요청 시 구독 취소 성공")
        @SuppressWarnings("unchecked")
        void withValidRequest_unsubscribesSuccess() {
            // given
            UserModel user = UserModelFixture.create();
            UUID playlistId = UUID.randomUUID();
            PlaylistModel playlistModel = PlaylistModelFixture.create();

            given(userService.getById(user.getId())).willReturn(user);
            given(playlistService.getById(playlistId)).willReturn(playlistModel);
            willAnswer(invocation -> {
                invocation.getArgument(0, Consumer.class).accept(null);
                return null;
            }).given(transactionTemplate).executeWithoutResult(any());
            willDoNothing().given(playlistSubscriptionService).unsubscribe(playlistId, user
                .getId());

            // when & then
            assertThatNoException()
                .isThrownBy(() -> playlistFacade.unsubscribePlaylist(user.getId(), playlistId));

            then(userService).should().getById(user.getId());
            then(playlistService).should().getById(playlistId);
            then(playlistSubscriptionService).should().unsubscribe(playlistId, user.getId());
        }
    }

    private static PlaylistResponse createPlaylistResponse(
        UUID id,
        long subscriberCount,
        boolean subscribedByMe
    ) {
        return createPlaylistResponse(id, "제목", "설명", subscriberCount, subscribedByMe);
    }

    private static PlaylistResponse createPlaylistResponse(
        UUID id,
        String title,
        String description,
        long subscriberCount,
        boolean subscribedByMe
    ) {
        return new PlaylistResponse(
            id, null, title, description, null, subscriberCount, subscribedByMe,
            Collections.emptyList()
        );
    }
}
