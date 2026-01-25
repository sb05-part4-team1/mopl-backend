package com.mopl.api.application.playlist;

import com.mopl.api.interfaces.api.playlist.dto.PlaylistCreateRequest;
import com.mopl.api.interfaces.api.playlist.dto.PlaylistUpdateRequest;
import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.exception.playlist.PlaylistForbiddenException;
import com.mopl.domain.exception.playlist.PlaylistNotFoundException;
import com.mopl.domain.fixture.ContentModelFixture;
import com.mopl.domain.fixture.PlaylistModelFixture;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.playlist.PlaylistQueryRequest;
import com.mopl.domain.repository.playlist.PlaylistSortField;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.outbox.OutboxService;
import com.mopl.domain.service.playlist.PlaylistService;
import com.mopl.domain.service.playlist.PlaylistSubscriptionService;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.dto.outbox.DomainEventOutboxMapper;
import com.mopl.dto.playlist.PlaylistResponse;
import com.mopl.dto.playlist.PlaylistResponseMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
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
    @SuppressWarnings("unused")
    private DomainEventOutboxMapper domainEventOutboxMapper;

    @Mock
    private OutboxService outboxService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private PlaylistFacade playlistFacade;

    @SuppressWarnings("unchecked")
    private void setupTransactionTemplate() {
        given(transactionTemplate.execute(any(TransactionCallback.class))).willAnswer(invocation -> {
            TransactionCallback<PlaylistModel> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock());
        });
    }

    private void setupTransactionTemplateWithoutResult() {
        willAnswer(invocation -> {
            invocation.<Consumer<Object>>getArgument(0).accept(null);
            return null;
        }).given(transactionTemplate).executeWithoutResult(any());
    }

    @Nested
    @DisplayName("getPlaylists()")
    class GetPlaylistsTest {

        @Test
        @DisplayName("플레이리스트 목록이 비어있으면 빈 응답 반환")
        void withEmptyPlaylists_returnsEmptyResponse() {
            // given
            UUID requesterId = UUID.randomUUID();
            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, 10, SortDirection.DESCENDING, PlaylistSortField.updatedAt
            );
            CursorResponse<PlaylistModel> emptyResponse = CursorResponse.empty(
                "updatedAt", SortDirection.DESCENDING
            );

            given(playlistService.getAll(request)).willReturn(emptyResponse);

            // when
            CursorResponse<PlaylistResponse> result = playlistFacade.getPlaylists(requesterId, request);

            // then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            then(playlistSubscriptionService).should(never()).findSubscribedPlaylistIds(any(), anyList());
            then(playlistService).should(never()).getContentsByPlaylistIdIn(anyList());
        }

        @Test
        @DisplayName("플레이리스트 목록이 있으면 구독 여부와 콘텐츠를 포함한 응답 반환")
        void withPlaylists_returnsResponseWithSubscriptionAndContents() {
            // given
            UUID requesterId = UUID.randomUUID();
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlist = PlaylistModelFixture.create(owner);
            UUID playlistId = playlist.getId();
            List<UUID> playlistIds = List.of(playlistId);

            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, 10, SortDirection.DESCENDING, PlaylistSortField.updatedAt
            );

            CursorResponse<PlaylistModel> playlistPage = CursorResponse.of(
                List.of(playlist),
                null,
                null,
                false,
                1L,
                "updatedAt",
                SortDirection.DESCENDING
            );

            Set<UUID> subscribedIds = Set.of(playlistId);
            Map<UUID, List<ContentModel>> contentsMap = Map.of(playlistId, Collections.emptyList());
            PlaylistResponse expectedResponse = mock(PlaylistResponse.class);

            given(playlistService.getAll(request)).willReturn(playlistPage);
            given(playlistSubscriptionService.findSubscribedPlaylistIds(requesterId, playlistIds))
                .willReturn(subscribedIds);
            given(playlistService.getContentsByPlaylistIdIn(playlistIds)).willReturn(contentsMap);
            given(playlistResponseMapper.toResponse(playlist, true, Collections.emptyList(), Map.of()))
                .willReturn(expectedResponse);

            // when
            CursorResponse<PlaylistResponse> result = playlistFacade.getPlaylists(requesterId, request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst()).isEqualTo(expectedResponse);
            then(playlistSubscriptionService).should().findSubscribedPlaylistIds(requesterId, playlistIds);
            then(playlistService).should().getContentsByPlaylistIdIn(playlistIds);
        }

        @Test
        @DisplayName("구독하지 않은 플레이리스트는 subscribedByMe가 false")
        void withUnsubscribedPlaylist_subscribedByMeIsFalse() {
            // given
            UUID requesterId = UUID.randomUUID();
            UserModel owner = UserModelFixture.create();
            PlaylistModel playlist = PlaylistModelFixture.create(owner);
            UUID playlistId = playlist.getId();
            List<UUID> playlistIds = List.of(playlistId);

            PlaylistQueryRequest request = new PlaylistQueryRequest(
                null, null, null, null, null, 10, SortDirection.DESCENDING, PlaylistSortField.updatedAt
            );

            CursorResponse<PlaylistModel> playlistPage = CursorResponse.of(
                List.of(playlist),
                null,
                null,
                false,
                1L,
                "updatedAt",
                SortDirection.DESCENDING
            );

            PlaylistResponse expectedResponse = mock(PlaylistResponse.class);

            given(playlistService.getAll(request)).willReturn(playlistPage);
            given(playlistSubscriptionService.findSubscribedPlaylistIds(requesterId, playlistIds))
                .willReturn(Collections.emptySet());
            given(playlistService.getContentsByPlaylistIdIn(playlistIds))
                .willReturn(Map.of(playlistId, Collections.emptyList()));
            given(playlistResponseMapper.toResponse(playlist, false, Collections.emptyList(), Map.of()))
                .willReturn(expectedResponse);

            // when
            CursorResponse<PlaylistResponse> result = playlistFacade.getPlaylists(requesterId, request);

            // then
            assertThat(result.data()).hasSize(1);
            then(playlistResponseMapper).should()
                .toResponse(playlist, false, Collections.emptyList(), Map.of());
        }
    }

    @Nested
    @DisplayName("getPlaylist()")
    class GetPlaylistTest {

        @Test
        @DisplayName("존재하는 플레이리스트 ID로 조회하면 PlaylistResponse 반환")
        void withExistingPlaylistId_returnsPlaylistResponse() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID requesterId = owner.getId();
            PlaylistModel playlist = PlaylistModelFixture.create(owner);
            UUID playlistId = playlist.getId();
            List<ContentModel> contents = List.of(ContentModelFixture.create());
            PlaylistResponse expectedResponse = mock(PlaylistResponse.class);

            given(userService.getById(requesterId)).willReturn(owner);
            given(playlistService.getById(playlistId)).willReturn(playlist);
            given(playlistSubscriptionService.isSubscribedByPlaylistIdAndSubscriberId(playlistId, requesterId))
                .willReturn(true);
            given(playlistService.getContentsByPlaylistId(playlistId)).willReturn(contents);
            given(playlistResponseMapper.toResponse(playlist, true, contents, Map.of()))
                .willReturn(expectedResponse);

            // when
            PlaylistResponse result = playlistFacade.getPlaylist(requesterId, playlistId);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(playlistService).should().getById(playlistId);
            then(playlistSubscriptionService).should()
                .isSubscribedByPlaylistIdAndSubscriberId(playlistId, requesterId);
            then(playlistService).should().getContentsByPlaylistId(playlistId);
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트 ID로 조회하면 예외 발생")
        void withNonExistingPlaylistId_throwsException() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID playlistId = UUID.randomUUID();

            given(userService.getById(requesterId)).willReturn(mock(UserModel.class));
            given(playlistService.getById(playlistId))
                .willThrow(PlaylistNotFoundException.withId(playlistId));

            // when & then
            assertThatThrownBy(() -> playlistFacade.getPlaylist(requesterId, playlistId))
                .isInstanceOf(PlaylistNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createPlaylist()")
    class CreatePlaylistTest {

        @Test
        @DisplayName("유효한 데이터가 주어지면 플레이리스트를 생성하고 응답을 반환한다")
        void withValidData_createsPlaylist() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID requesterId = owner.getId();
            PlaylistCreateRequest request = new PlaylistCreateRequest("My Playlist", "Description");

            PlaylistModel createdPlaylist = PlaylistModelFixture.create(owner);
            PlaylistResponse expectedResponse = mock(PlaylistResponse.class);

            given(userService.getById(requesterId)).willReturn(owner);
            given(playlistService.create(any(PlaylistModel.class))).willReturn(createdPlaylist);
            given(playlistResponseMapper.toResponse(createdPlaylist)).willReturn(expectedResponse);
            setupTransactionTemplate();

            // when
            PlaylistResponse result = playlistFacade.createPlaylist(requesterId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(playlistService).should().create(any(PlaylistModel.class));
            then(outboxService).should().save(any());
        }
    }

    @Nested
    @DisplayName("updatePlaylist()")
    class UpdatePlaylistTest {

        @Test
        @DisplayName("소유자가 플레이리스트를 수정하면 성공한다")
        void withOwner_updatesPlaylist() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID requesterId = owner.getId();
            PlaylistModel playlist = PlaylistModelFixture.create(owner);
            UUID playlistId = playlist.getId();
            PlaylistUpdateRequest request = new PlaylistUpdateRequest("Updated Title", "Updated Description");

            PlaylistModel updatedPlaylist = playlist.update(request.title(), request.description());
            List<ContentModel> contents = List.of(ContentModelFixture.create());
            PlaylistResponse expectedResponse = mock(PlaylistResponse.class);

            given(userService.getById(requesterId)).willReturn(owner);
            given(playlistService.getById(playlistId)).willReturn(playlist);
            given(playlistService.update(any(PlaylistModel.class))).willReturn(updatedPlaylist);
            given(playlistSubscriptionService.isSubscribedByPlaylistIdAndSubscriberId(playlistId, requesterId))
                .willReturn(true);
            given(playlistService.getContentsByPlaylistId(playlistId)).willReturn(contents);
            given(playlistResponseMapper.toResponse(updatedPlaylist, true, contents, Map.of()))
                .willReturn(expectedResponse);
            setupTransactionTemplate();

            // when
            PlaylistResponse result = playlistFacade.updatePlaylist(requesterId, playlistId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(playlistService).should().update(any(PlaylistModel.class));
            then(outboxService).should().save(any());
        }

        @Test
        @DisplayName("소유자가 아닌 사용자가 수정하면 예외 발생")
        void withNonOwner_throwsException() {
            // given
            UserModel owner = UserModelFixture.create();
            UserModel nonOwner = UserModelFixture.create();
            UUID requesterId = nonOwner.getId();
            PlaylistModel playlist = PlaylistModelFixture.create(owner);
            UUID playlistId = playlist.getId();
            PlaylistUpdateRequest request = new PlaylistUpdateRequest("Updated Title", null);

            given(userService.getById(requesterId)).willReturn(nonOwner);
            given(playlistService.getById(playlistId)).willReturn(playlist);

            // when & then
            assertThatThrownBy(() -> playlistFacade.updatePlaylist(requesterId, playlistId, request))
                .isInstanceOf(PlaylistForbiddenException.class);

            then(playlistService).should(never()).update(any(PlaylistModel.class));
        }

        @Test
        @DisplayName("title만 수정해도 성공한다")
        void withOnlyTitle_updatesSuccessfully() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID requesterId = owner.getId();
            PlaylistModel playlist = PlaylistModelFixture.create(owner);
            UUID playlistId = playlist.getId();
            PlaylistUpdateRequest request = new PlaylistUpdateRequest("Updated Title", null);

            PlaylistModel updatedPlaylist = playlist.update(request.title(), request.description());
            List<ContentModel> contents = Collections.emptyList();
            PlaylistResponse expectedResponse = mock(PlaylistResponse.class);

            given(userService.getById(requesterId)).willReturn(owner);
            given(playlistService.getById(playlistId)).willReturn(playlist);
            given(playlistService.update(any(PlaylistModel.class))).willReturn(updatedPlaylist);
            given(playlistSubscriptionService.isSubscribedByPlaylistIdAndSubscriberId(playlistId, requesterId))
                .willReturn(false);
            given(playlistService.getContentsByPlaylistId(playlistId)).willReturn(contents);
            given(playlistResponseMapper.toResponse(updatedPlaylist, false, contents, Map.of()))
                .willReturn(expectedResponse);
            setupTransactionTemplate();

            // when
            PlaylistResponse result = playlistFacade.updatePlaylist(requesterId, playlistId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("description만 수정해도 성공한다")
        void withOnlyDescription_updatesSuccessfully() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID requesterId = owner.getId();
            PlaylistModel playlist = PlaylistModelFixture.create(owner);
            UUID playlistId = playlist.getId();
            PlaylistUpdateRequest request = new PlaylistUpdateRequest(null, "Updated Description");

            PlaylistModel updatedPlaylist = playlist.update(request.title(), request.description());
            List<ContentModel> contents = Collections.emptyList();
            PlaylistResponse expectedResponse = mock(PlaylistResponse.class);

            given(userService.getById(requesterId)).willReturn(owner);
            given(playlistService.getById(playlistId)).willReturn(playlist);
            given(playlistService.update(any(PlaylistModel.class))).willReturn(updatedPlaylist);
            given(playlistSubscriptionService.isSubscribedByPlaylistIdAndSubscriberId(playlistId, requesterId))
                .willReturn(false);
            given(playlistService.getContentsByPlaylistId(playlistId)).willReturn(contents);
            given(playlistResponseMapper.toResponse(updatedPlaylist, false, contents, Map.of()))
                .willReturn(expectedResponse);
            setupTransactionTemplate();

            // when
            PlaylistResponse result = playlistFacade.updatePlaylist(requesterId, playlistId, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
        }
    }

    @Nested
    @DisplayName("deletePlaylist()")
    class DeletePlaylistTest {

        @Test
        @DisplayName("소유자가 플레이리스트를 삭제하면 성공한다")
        void withOwner_deletesPlaylist() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID requesterId = owner.getId();
            PlaylistModel playlist = PlaylistModelFixture.create(owner);
            UUID playlistId = playlist.getId();

            given(userService.getById(requesterId)).willReturn(owner);
            given(playlistService.getById(playlistId)).willReturn(playlist);
            willDoNothing().given(playlistService).delete(playlist);
            setupTransactionTemplateWithoutResult();

            // when & then
            assertThatNoException()
                .isThrownBy(() -> playlistFacade.deletePlaylist(requesterId, playlistId));

            then(playlistService).should().delete(playlist);
        }

        @Test
        @DisplayName("소유자가 아닌 사용자가 삭제하면 예외 발생")
        void withNonOwner_throwsException() {
            // given
            UserModel owner = UserModelFixture.create();
            UserModel nonOwner = UserModelFixture.create();
            UUID requesterId = nonOwner.getId();
            PlaylistModel playlist = PlaylistModelFixture.create(owner);
            UUID playlistId = playlist.getId();

            given(userService.getById(requesterId)).willReturn(nonOwner);
            given(playlistService.getById(playlistId)).willReturn(playlist);

            // when & then
            assertThatThrownBy(() -> playlistFacade.deletePlaylist(requesterId, playlistId))
                .isInstanceOf(PlaylistForbiddenException.class);

            then(playlistService).should(never()).delete(any(PlaylistModel.class));
        }
    }

    @Nested
    @DisplayName("addContentToPlaylist()")
    class AddContentToPlaylistTest {

        @Test
        @DisplayName("소유자가 콘텐츠를 추가하면 성공한다")
        void withOwner_addsContent() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID requesterId = owner.getId();
            PlaylistModel playlist = PlaylistModelFixture.create(owner);
            UUID playlistId = playlist.getId();
            ContentModel content = ContentModelFixture.create();
            UUID contentId = content.getId();

            given(userService.getById(requesterId)).willReturn(owner);
            given(playlistService.getById(playlistId)).willReturn(playlist);
            given(contentService.getById(contentId)).willReturn(content);
            willDoNothing().given(playlistService).addContent(playlistId, contentId);
            setupTransactionTemplateWithoutResult();

            // when & then
            assertThatNoException()
                .isThrownBy(() -> playlistFacade.addContentToPlaylist(requesterId, playlistId, contentId));

            then(playlistService).should().addContent(playlistId, contentId);
            then(outboxService).should().save(any());
        }

        @Test
        @DisplayName("소유자가 아닌 사용자가 콘텐츠를 추가하면 예외 발생")
        void withNonOwner_throwsException() {
            // given
            UserModel owner = UserModelFixture.create();
            UserModel nonOwner = UserModelFixture.create();
            UUID requesterId = nonOwner.getId();
            PlaylistModel playlist = PlaylistModelFixture.create(owner);
            UUID playlistId = playlist.getId();
            UUID contentId = UUID.randomUUID();

            given(userService.getById(requesterId)).willReturn(nonOwner);
            given(playlistService.getById(playlistId)).willReturn(playlist);

            // when & then
            assertThatThrownBy(() -> playlistFacade.addContentToPlaylist(requesterId, playlistId, contentId))
                .isInstanceOf(PlaylistForbiddenException.class);

            then(playlistService).should(never()).addContent(any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠를 추가하면 예외 발생")
        void withNonExistingContent_throwsException() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID requesterId = owner.getId();
            PlaylistModel playlist = PlaylistModelFixture.create(owner);
            UUID playlistId = playlist.getId();
            UUID contentId = UUID.randomUUID();

            given(userService.getById(requesterId)).willReturn(owner);
            given(playlistService.getById(playlistId)).willReturn(playlist);
            given(contentService.getById(contentId))
                .willThrow(ContentNotFoundException.withId(contentId));

            // when & then
            assertThatThrownBy(() -> playlistFacade.addContentToPlaylist(requesterId, playlistId, contentId))
                .isInstanceOf(ContentNotFoundException.class);

            then(playlistService).should(never()).addContent(any(), any());
        }
    }

    @Nested
    @DisplayName("deleteContentFromPlaylist()")
    class DeleteContentFromPlaylistTest {

        @Test
        @DisplayName("소유자가 콘텐츠를 삭제하면 성공한다")
        void withOwner_deletesContent() {
            // given
            UserModel owner = UserModelFixture.create();
            UUID requesterId = owner.getId();
            PlaylistModel playlist = PlaylistModelFixture.create(owner);
            UUID playlistId = playlist.getId();
            UUID contentId = UUID.randomUUID();

            given(userService.getById(requesterId)).willReturn(owner);
            given(playlistService.getById(playlistId)).willReturn(playlist);
            willDoNothing().given(playlistService).deleteContentFromPlaylist(playlistId, contentId);
            setupTransactionTemplateWithoutResult();

            // when & then
            assertThatNoException()
                .isThrownBy(() -> playlistFacade.deleteContentFromPlaylist(requesterId, playlistId, contentId));

            then(playlistService).should().deleteContentFromPlaylist(playlistId, contentId);
        }

        @Test
        @DisplayName("소유자가 아닌 사용자가 콘텐츠를 삭제하면 예외 발생")
        void withNonOwner_throwsException() {
            // given
            UserModel owner = UserModelFixture.create();
            UserModel nonOwner = UserModelFixture.create();
            UUID requesterId = nonOwner.getId();
            PlaylistModel playlist = PlaylistModelFixture.create(owner);
            UUID playlistId = playlist.getId();
            UUID contentId = UUID.randomUUID();

            given(userService.getById(requesterId)).willReturn(nonOwner);
            given(playlistService.getById(playlistId)).willReturn(playlist);

            // when & then
            assertThatThrownBy(() -> playlistFacade.deleteContentFromPlaylist(requesterId, playlistId, contentId))
                .isInstanceOf(PlaylistForbiddenException.class);

            then(playlistService).should(never()).deleteContentFromPlaylist(any(), any());
        }
    }

    @Nested
    @DisplayName("subscribePlaylist()")
    class SubscribePlaylistTest {

        @Test
        @DisplayName("유효한 요청이면 구독에 성공한다")
        void withValidRequest_subscribesSuccessfully() {
            // given
            UserModel subscriber = UserModelFixture.create();
            UUID requesterId = subscriber.getId();
            PlaylistModel playlist = PlaylistModelFixture.create();
            UUID playlistId = playlist.getId();

            given(userService.getById(requesterId)).willReturn(subscriber);
            given(playlistService.getById(playlistId)).willReturn(playlist);
            willDoNothing().given(playlistSubscriptionService).subscribe(playlistId, requesterId);
            given(playlistService.update(any(PlaylistModel.class))).willReturn(playlist.withSubscriberAdded());
            setupTransactionTemplateWithoutResult();

            // when & then
            assertThatNoException()
                .isThrownBy(() -> playlistFacade.subscribePlaylist(requesterId, playlistId));

            then(playlistSubscriptionService).should().subscribe(playlistId, requesterId);
            then(playlistService).should().update(any(PlaylistModel.class));
            then(outboxService).should().save(any());
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트 구독 시 예외 발생")
        void withNonExistingPlaylist_throwsException() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID playlistId = UUID.randomUUID();

            given(userService.getById(requesterId)).willReturn(mock(UserModel.class));
            given(playlistService.getById(playlistId))
                .willThrow(PlaylistNotFoundException.withId(playlistId));

            // when & then
            assertThatThrownBy(() -> playlistFacade.subscribePlaylist(requesterId, playlistId))
                .isInstanceOf(PlaylistNotFoundException.class);

            then(playlistSubscriptionService).should(never()).subscribe(any(), any());
        }
    }

    @Nested
    @DisplayName("unsubscribePlaylist()")
    class UnsubscribePlaylistTest {

        @Test
        @DisplayName("유효한 요청이면 구독 취소에 성공한다")
        void withValidRequest_unsubscribesSuccessfully() {
            // given
            UserModel subscriber = UserModelFixture.create();
            UUID requesterId = subscriber.getId();
            PlaylistModel playlist = PlaylistModelFixture.builder()
                .set("subscriberCount", 1)
                .sample();
            UUID playlistId = playlist.getId();

            given(userService.getById(requesterId)).willReturn(subscriber);
            given(playlistService.getById(playlistId)).willReturn(playlist);
            willDoNothing().given(playlistSubscriptionService).unsubscribe(playlistId, requesterId);
            given(playlistService.update(any(PlaylistModel.class))).willReturn(playlist.withSubscriberRemoved());
            setupTransactionTemplateWithoutResult();

            // when & then
            assertThatNoException()
                .isThrownBy(() -> playlistFacade.unsubscribePlaylist(requesterId, playlistId));

            then(playlistSubscriptionService).should().unsubscribe(playlistId, requesterId);
            then(playlistService).should().update(any(PlaylistModel.class));
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트 구독 취소 시 예외 발생")
        void withNonExistingPlaylist_throwsException() {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID playlistId = UUID.randomUUID();

            given(userService.getById(requesterId)).willReturn(mock(UserModel.class));
            given(playlistService.getById(playlistId))
                .willThrow(PlaylistNotFoundException.withId(playlistId));

            // when & then
            assertThatThrownBy(() -> playlistFacade.unsubscribePlaylist(requesterId, playlistId))
                .isInstanceOf(PlaylistNotFoundException.class);

            then(playlistSubscriptionService).should(never()).unsubscribe(any(), any());
        }
    }
}
