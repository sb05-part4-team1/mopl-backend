// package com.mopl.api.application.playlist;
//
// import com.mopl.api.application.outbox.DomainEventOutboxMapper;
// import com.mopl.api.interfaces.api.playlist.PlaylistCreateRequest;
// import com.mopl.api.interfaces.api.playlist.PlaylistResponse;
// import com.mopl.api.interfaces.api.playlist.PlaylistResponseMapper;
// import com.mopl.api.interfaces.api.playlist.PlaylistUpdateRequest;
// import com.mopl.domain.exception.content.ContentNotFoundException;
// import com.mopl.domain.exception.playlist.PlaylistForbiddenException;
// import com.mopl.domain.fixture.ContentModelFixture;
// import com.mopl.domain.fixture.PlaylistModelFixture;
// import com.mopl.domain.fixture.UserModelFixture;
// import com.mopl.domain.model.content.ContentModel;
// import com.mopl.domain.model.playlist.PlaylistModel;
// import com.mopl.domain.model.user.UserModel;
// import com.mopl.domain.repository.playlist.PlaylistQueryRequest;
// import com.mopl.domain.service.content.ContentService;
// import com.mopl.domain.service.outbox.OutboxService;
// import com.mopl.domain.service.playlist.PlaylistService;
// import com.mopl.domain.service.playlist.PlaylistSubscriptionService;
// import com.mopl.domain.service.user.UserService;
// import com.mopl.domain.support.cursor.CursorResponse;
// import com.mopl.domain.support.cursor.SortDirection;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.transaction.TransactionStatus;
// import org.springframework.transaction.support.TransactionTemplate;
//
// import java.util.Collections;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
// import java.util.UUID;
// import java.util.function.Consumer;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatNoException;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.BDDMockito.given;
// import static org.mockito.BDDMockito.then;
// import static org.mockito.BDDMockito.willAnswer;
// import static org.mockito.BDDMockito.willDoNothing;
// import static org.mockito.Mockito.never;
//
// @ExtendWith(MockitoExtension.class)
// @DisplayName("PlaylistFacade 단위 테스트")
// class PlaylistFacadeTest {
//
//     @Mock
//     private PlaylistService playlistService;
//
//     @Mock
//     private PlaylistSubscriptionService playlistSubscriptionService;
//
//     @Mock
//     private UserService userService;
//
//     @Mock
//     private ContentService contentService;
//
//     @Mock
//     private PlaylistResponseMapper playlistResponseMapper;
//
//     @Mock
//     private TransactionTemplate transactionTemplate;
//
//     @Mock
//     private DomainEventOutboxMapper domainEventOutboxMapper;
//
//     @Mock
//     private OutboxService outboxService;
//
//     @InjectMocks
//     private PlaylistFacade playlistFacade;
//
//     @Nested
//     @DisplayName("getPlaylists()")
//     class GetPlaylistsTest {
//
//         @Test
//         @DisplayName("유효한 요청 시 플레이리스트 목록 조회 성공")
//         void withValidRequest_getsPlaylistsSuccess() {
//             // given
//             UserModel owner = UserModelFixture.create();
//             UUID requesterId = owner.getId();
//             PlaylistModel playlistModel = PlaylistModelFixture.builder(owner).sample();
//             UUID playlistId = playlistModel.getId();
//             List<UUID> playlistIds = List.of(playlistId);
//             long subscriberCount = 10L;
//
//             PlaylistQueryRequest request = new PlaylistQueryRequest(
//                 null, null, null, null, null, 10, SortDirection.ASCENDING, null
//             );
//
//             CursorResponse<PlaylistModel> playlistPage = CursorResponse.of(
//                 List.of(playlistModel),
//                 null,
//                 null,
//                 false,
//                 1L,
//                 "updatedAt",
//                 SortDirection.ASCENDING
//             );
//
//             PlaylistResponse expectedResponse = createPlaylistResponse(
//                 playlistId, subscriberCount, true
//             );
//
//             given(playlistService.getAll(request)).willReturn(playlistPage);
//             given(playlistSubscriptionService.getSubscriberCounts(playlistIds))
//                 .willReturn(Map.of(playlistId, subscriberCount));
//             given(playlistSubscriptionService.findSubscribedPlaylistIds(requesterId, playlistIds))
//                 .willReturn(Set.of(playlistId));
//             given(playlistService.getContentsByPlaylistIdIn(playlistIds))
//                 .willReturn(Map.of(playlistId, Collections.emptyList()));
//             given(playlistResponseMapper.toResponse(
//                 playlistModel, subscriberCount, true, Collections.emptyList()
//             )).willReturn(expectedResponse);
//
//             // when
//             CursorResponse<PlaylistResponse> result = playlistFacade.getPlaylists(
//                 requesterId, request
//             );
//
//             // then
//             assertThat(result.data()).hasSize(1);
//             assertThat(result.data().getFirst())
//                 .usingRecursiveComparison()
//                 .isEqualTo(expectedResponse);
//
//             then(playlistService).should().getAll(request);
//             then(playlistSubscriptionService).should().getSubscriberCounts(playlistIds);
//             then(playlistSubscriptionService).should()
//                 .findSubscribedPlaylistIds(requesterId, playlistIds);
//             then(playlistService).should().getContentsByPlaylistIdIn(playlistIds);
//         }
//
//         @Test
//         @DisplayName("빈 목록 시 빈 응답 반환")
//         void withEmptyList_returnsEmptyResponse() {
//             // given
//             UUID requesterId = UUID.randomUUID();
//             PlaylistQueryRequest request = new PlaylistQueryRequest(
//                 null, null, null, null, null, 10, SortDirection.ASCENDING, null
//             );
//
//             CursorResponse<PlaylistModel> emptyPage = CursorResponse.empty(
//                 "updatedAt", SortDirection.ASCENDING
//             );
//
//             given(playlistService.getAll(request)).willReturn(emptyPage);
//
//             // when
//             CursorResponse<PlaylistResponse> result = playlistFacade.getPlaylists(
//                 requesterId, request
//             );
//
//             // then
//             assertThat(result.data()).isEmpty();
//
//             then(playlistSubscriptionService).should(never()).getSubscriberCounts(any());
//             then(playlistSubscriptionService).should(never())
//                 .findSubscribedPlaylistIds(any(), any());
//             then(playlistService).should(never()).getContentsByPlaylistIdIn(any());
//         }
//
//         @Test
//         @DisplayName("비로그인 사용자(requesterId가 null)일 때 구독 여부는 모두 false")
//         void withNullRequesterId_subscribedByMeIsFalse() {
//             // given
//             UserModel owner = UserModelFixture.create();
//             PlaylistModel playlistModel = PlaylistModelFixture.builder(owner).sample();
//             UUID playlistId = playlistModel.getId();
//             List<UUID> playlistIds = List.of(playlistId);
//             long subscriberCount = 5L;
//
//             PlaylistQueryRequest request = new PlaylistQueryRequest(
//                 null, null, null, null, null, 10, SortDirection.ASCENDING, null
//             );
//
//             CursorResponse<PlaylistModel> playlistPage = CursorResponse.of(
//                 List.of(playlistModel),
//                 null,
//                 null,
//                 false,
//                 1L,
//                 "updatedAt",
//                 SortDirection.ASCENDING
//             );
//
//             PlaylistResponse expectedResponse = createPlaylistResponse(
//                 playlistId, subscriberCount, false
//             );
//
//             given(playlistService.getAll(request)).willReturn(playlistPage);
//             given(playlistSubscriptionService.getSubscriberCounts(playlistIds))
//                 .willReturn(Map.of(playlistId, subscriberCount));
//             given(playlistSubscriptionService.findSubscribedPlaylistIds(null, playlistIds))
//                 .willReturn(Collections.emptySet());
//             given(playlistService.getContentsByPlaylistIdIn(playlistIds))
//                 .willReturn(Map.of(playlistId, Collections.emptyList()));
//             given(playlistResponseMapper.toResponse(
//                 playlistModel, subscriberCount, false, Collections.emptyList()
//             )).willReturn(expectedResponse);
//
//             // when
//             CursorResponse<PlaylistResponse> result = playlistFacade.getPlaylists(null, request);
//
//             // then
//             assertThat(result.data()).hasSize(1);
//             assertThat(result.data().getFirst())
//                 .usingRecursiveComparison()
//                 .isEqualTo(expectedResponse);
//
//             then(playlistSubscriptionService).should()
//                 .findSubscribedPlaylistIds(null, playlistIds);
//         }
//     }
//
//     @Nested
//     @DisplayName("getPlaylist()")
//     class GetPlaylistTest {
//
//         @Test
//         @DisplayName("유효한 요청 시 플레이리스트 상세 정보 조회 성공")
//         void withValidRequest_getsPlaylistSuccess() {
//             // given
//             UserModel owner = UserModelFixture.create();
//             UUID playlistId = UUID.randomUUID();
//             PlaylistModel playlistModel = PlaylistModelFixture.builder(owner)
//                 .set("id", playlistId)
//                 .sample();
//             List<ContentModel> contents = List.of(ContentModelFixture.create());
//             long subscriberCount = 10L;
//             boolean subscribedByMe = true;
//
//             PlaylistResponse expectedResponse = createPlaylistResponse(
//                 playlistId, playlistModel.getTitle(), playlistModel.getDescription(),
//                 subscriberCount, subscribedByMe
//             );
//
//             given(userService.getById(owner.getId())).willReturn(owner);
//             given(playlistService.getById(playlistId)).willReturn(playlistModel);
//             given(playlistSubscriptionService.getSubscriberCount(playlistId))
//                 .willReturn(subscriberCount);
//             given(playlistSubscriptionService.isSubscribedByPlaylistIdAndSubscriberId(
//                 playlistId, owner.getId()
//             )).willReturn(subscribedByMe);
//             given(playlistService.getContentsByPlaylistId(playlistId)).willReturn(contents);
//             given(playlistResponseMapper.toResponse(
//                 playlistModel, subscriberCount, subscribedByMe, contents
//             )).willReturn(expectedResponse);
//
//             // when
//             PlaylistResponse result = playlistFacade.getPlaylist(owner.getId(), playlistId);
//
//             // then
//             assertThat(result)
//                 .usingRecursiveComparison()
//                 .isEqualTo(expectedResponse);
//
//             then(userService).should().getById(owner.getId());
//             then(playlistService).should().getById(playlistId);
//             then(playlistSubscriptionService).should().getSubscriberCount(playlistId);
//             then(playlistSubscriptionService).should()
//                 .isSubscribedByPlaylistIdAndSubscriberId(playlistId, owner.getId());
//             then(playlistService).should().getContentsByPlaylistId(playlistId);
//             then(playlistResponseMapper).should()
//                 .toResponse(playlistModel, subscriberCount, subscribedByMe, contents);
//         }
//     }
//
//     @Nested
//     @DisplayName("createPlaylist()")
//     class CreatePlaylistTest {
//
//         @Test
//         @DisplayName("유효한 요청 시 플레이리스트 생성 성공")
//         void withValidRequest_createsPlaylistSuccess() {
//             // given
//             UserModel owner = UserModelFixture.create();
//             String title = "내 플레이리스트";
//             String description = "플레이리스트 설명";
//             PlaylistCreateRequest request = new PlaylistCreateRequest(title, description);
//
//             PlaylistModel playlistModel = PlaylistModelFixture.builder(owner)
//                 .set("title", title)
//                 .set("description", description)
//                 .sample();
//
//             PlaylistResponse expectedResponse = createPlaylistResponse(
//                 playlistModel.getId(), title, description, 0L, false
//             );
//
//             given(userService.getById(owner.getId())).willReturn(owner);
//             given(playlistService.create(any(PlaylistModel.class))).willReturn(playlistModel);
//             given(playlistResponseMapper.toResponse(playlistModel)).willReturn(expectedResponse);
//             willAnswer(invocation -> invocation.<org.springframework.transaction.support.TransactionCallback<?>>getArgument(
//                 0)
//                 .doInTransaction(org.mockito.Mockito.mock(TransactionStatus.class)))
//                 .given(transactionTemplate).execute(any());
//
//             // when
//             PlaylistResponse result = playlistFacade.createPlaylist(owner.getId(), request);
//
//             // then
//             assertThat(result)
//                 .usingRecursiveComparison()
//                 .isEqualTo(expectedResponse);
//
//             then(userService).should().getById(owner.getId());
//             then(playlistService).should().create(any(PlaylistModel.class));
//             then(playlistResponseMapper).should().toResponse(playlistModel);
//         }
//     }
//
//     @Nested
//     @DisplayName("updatePlaylist()")
//     class UpdatePlaylistTest {
//
//         @Test
//         @DisplayName("소유자가 플레이리스트 수정 성공")
//         void withOwner_updatesPlaylistSuccess() {
//             // given
//             UserModel owner = UserModelFixture.create();
//             UUID playlistId = UUID.randomUUID();
//             String newTitle = "수정된 제목";
//             String newDescription = "수정된 설명";
//             PlaylistUpdateRequest request = new PlaylistUpdateRequest(newTitle, newDescription);
//
//             PlaylistModel originalPlaylist = PlaylistModelFixture.builder(owner)
//                 .set("id", playlistId)
//                 .sample();
//
//             PlaylistModel updatedPlaylist = PlaylistModelFixture.builder(owner)
//                 .set("id", playlistId)
//                 .set("title", newTitle)
//                 .set("description", newDescription)
//                 .sample();
//
//             PlaylistResponse expectedResponse = createPlaylistResponse(
//                 playlistId, newTitle, newDescription, 0L, false
//             );
//
//             given(userService.getById(owner.getId())).willReturn(owner);
//             given(playlistService.getById(playlistId)).willReturn(originalPlaylist);
//             given(playlistService.update(any(PlaylistModel.class))).willReturn(updatedPlaylist);
//             given(playlistResponseMapper.toResponse(updatedPlaylist)).willReturn(expectedResponse);
//             willAnswer(invocation -> invocation.<org.springframework.transaction.support.TransactionCallback<?>>getArgument(
//                 0)
//                 .doInTransaction(org.mockito.Mockito.mock(TransactionStatus.class)))
//                 .given(transactionTemplate).execute(any());
//
//             // when
//             PlaylistResponse result = playlistFacade.updatePlaylist(
//                 owner.getId(), playlistId, request
//             );
//
//             // then
//             assertThat(result)
//                 .usingRecursiveComparison()
//                 .isEqualTo(expectedResponse);
//
//             then(userService).should().getById(owner.getId());
//             then(playlistService).should().getById(playlistId);
//             then(playlistService).should().update(any(PlaylistModel.class));
//             then(playlistResponseMapper).should().toResponse(updatedPlaylist);
//         }
//
//         @Test
//         @DisplayName("소유자가 아닌 사용자가 수정 시 PlaylistForbiddenException 발생")
//         void withNonOwner_throwsPlaylistForbiddenException() {
//             // given
//             UserModel owner = UserModelFixture.create();
//             UUID playlistId = UUID.randomUUID();
//             UUID nonOwnerId = UUID.randomUUID();
//             PlaylistUpdateRequest request = new PlaylistUpdateRequest("새 제목", "새 설명");
//
//             PlaylistModel playlist = PlaylistModelFixture.builder(owner)
//                 .set("id", playlistId)
//                 .sample();
//
//             given(userService.getById(nonOwnerId)).willReturn(
//                 UserModelFixture.builder().set("id", nonOwnerId).sample()
//             );
//             given(playlistService.getById(playlistId)).willReturn(playlist);
//
//             // when & then
//             assertThatThrownBy(
//                 () -> playlistFacade.updatePlaylist(nonOwnerId, playlistId, request)
//             )
//                 .isInstanceOf(PlaylistForbiddenException.class)
//                 .satisfies(e -> {
//                     PlaylistForbiddenException ex = (PlaylistForbiddenException) e;
//                     assertThat(ex.getDetails().get("playlistId")).isEqualTo(playlistId);
//                     assertThat(ex.getDetails().get("requesterId")).isEqualTo(nonOwnerId);
//                     assertThat(ex.getDetails().get("ownerId")).isEqualTo(owner.getId());
//                 });
//
//             then(playlistService).should(never()).update(any(PlaylistModel.class));
//         }
//     }
//
//     @Nested
//     @DisplayName("deletePlaylist()")
//     class DeletePlaylistTest {
//
//         @Test
//         @DisplayName("소유자가 플레이리스트 삭제 성공")
//         void withOwner_deletesPlaylistSuccess() {
//             // given
//             UserModel owner = UserModelFixture.create();
//             UUID playlistId = UUID.randomUUID();
//
//             PlaylistModel playlist = PlaylistModelFixture.builder(owner)
//                 .set("id", playlistId)
//                 .sample();
//
//             given(userService.getById(owner.getId())).willReturn(owner);
//             given(playlistService.getById(playlistId)).willReturn(playlist);
//             willDoNothing().given(playlistService).delete(playlist);
//
//             // when & then
//             assertThatNoException()
//                 .isThrownBy(() -> playlistFacade.deletePlaylist(owner.getId(), playlistId));
//
//             then(userService).should().getById(owner.getId());
//             then(playlistService).should().getById(playlistId);
//             then(playlistService).should().delete(playlist);
//         }
//
//         @Test
//         @DisplayName("소유자가 아닌 사용자가 삭제 시 PlaylistForbiddenException 발생")
//         void withNonOwner_throwsPlaylistForbiddenException() {
//             // given
//             UserModel owner = UserModelFixture.create();
//             UUID playlistId = UUID.randomUUID();
//             UUID nonOwnerId = UUID.randomUUID();
//
//             PlaylistModel playlist = PlaylistModelFixture.builder(owner)
//                 .set("id", playlistId)
//                 .sample();
//
//             given(userService.getById(nonOwnerId)).willReturn(
//                 UserModelFixture.builder().set("id", nonOwnerId).sample()
//             );
//             given(playlistService.getById(playlistId)).willReturn(playlist);
//
//             // when & then
//             assertThatThrownBy(() -> playlistFacade.deletePlaylist(nonOwnerId, playlistId))
//                 .isInstanceOf(PlaylistForbiddenException.class)
//                 .satisfies(e -> {
//                     PlaylistForbiddenException ex = (PlaylistForbiddenException) e;
//                     assertThat(ex.getDetails().get("playlistId")).isEqualTo(playlistId);
//                     assertThat(ex.getDetails().get("requesterId")).isEqualTo(nonOwnerId);
//                     assertThat(ex.getDetails().get("ownerId")).isEqualTo(owner.getId());
//                 });
//
//             then(playlistService).should(never()).delete(any(PlaylistModel.class));
//         }
//     }
//
//     @Nested
//     @DisplayName("addContentToPlaylist()")
//     class AddContentToPlaylistTest {
//
//         @Test
//         @DisplayName("소유자가 콘텐츠 추가 성공")
//         void withOwner_addsContentSuccess() {
//             // given
//             UserModel owner = UserModelFixture.create();
//             UUID playlistId = UUID.randomUUID();
//             PlaylistModel playlistModel = PlaylistModelFixture.builder(owner)
//                 .set("id", playlistId)
//                 .sample();
//             ContentModel contentModel = ContentModelFixture.create();
//             UUID contentId = contentModel.getId();
//
//             given(userService.getById(owner.getId())).willReturn(owner);
//             given(playlistService.getById(playlistId)).willReturn(playlistModel);
//             given(contentService.getById(contentId)).willReturn(contentModel);
//             willDoNothing().given(playlistService).addContent(playlistId, contentId);
//             willAnswer(invocation -> {
//                 invocation.<Consumer<Object>>getArgument(0).accept(null);
//                 return null;
//             }).given(transactionTemplate).executeWithoutResult(any());
//
//             // when & then
//             assertThatNoException()
//                 .isThrownBy(() -> playlistFacade.addContentToPlaylist(owner.getId(), playlistId,
//                     contentId));
//
//             then(userService).should().getById(owner.getId());
//             then(playlistService).should().getById(playlistId);
//             then(playlistService).should().addContent(playlistId, contentId);
//         }
//
//         @Test
//         @DisplayName("소유자가 아닌 사용자가 콘텐츠 추가 시 PlaylistForbiddenException 발생")
//         void withNonOwner_throwsPlaylistForbiddenException() {
//             // given
//             UserModel owner = UserModelFixture.create();
//             UUID playlistId = UUID.randomUUID();
//             UUID nonOwnerId = UUID.randomUUID();
//             UUID contentId = UUID.randomUUID();
//
//             PlaylistModel playlist = PlaylistModelFixture.builder(owner)
//                 .set("id", playlistId)
//                 .sample();
//
//             given(userService.getById(nonOwnerId)).willReturn(
//                 UserModelFixture.builder().set("id", nonOwnerId).sample()
//             );
//             given(playlistService.getById(playlistId)).willReturn(playlist);
//
//             // when & then
//             assertThatThrownBy(
//                 () -> playlistFacade.addContentToPlaylist(nonOwnerId, playlistId, contentId)
//             )
//                 .isInstanceOf(PlaylistForbiddenException.class)
//                 .satisfies(e -> {
//                     PlaylistForbiddenException ex = (PlaylistForbiddenException) e;
//                     assertThat(ex.getDetails().get("playlistId")).isEqualTo(playlistId);
//                     assertThat(ex.getDetails().get("requesterId")).isEqualTo(nonOwnerId);
//                     assertThat(ex.getDetails().get("ownerId")).isEqualTo(owner.getId());
//                 });
//
//             then(playlistService).should(never()).addContent(any(UUID.class), any(UUID.class));
//         }
//
//         @Test
//         @DisplayName("존재하지 않는 콘텐츠 추가 시 ContentNotFoundException 발생")
//         void withNonExistingContent_throwsContentNotFoundException() {
//             // given
//             UserModel owner = UserModelFixture.create();
//             UUID playlistId = UUID.randomUUID();
//             UUID contentId = UUID.randomUUID();
//
//             PlaylistModel playlist = PlaylistModelFixture.builder(owner)
//                 .set("id", playlistId)
//                 .sample();
//
//             given(userService.getById(owner.getId())).willReturn(owner);
//             given(playlistService.getById(playlistId)).willReturn(playlist);
//
//             // when & then
//             assertThatThrownBy(() -> playlistFacade.addContentToPlaylist(owner.getId(), playlistId,
//                 contentId))
//                 .isInstanceOf(ContentNotFoundException.class)
//                 .satisfies(e -> {
//                     ContentNotFoundException ex = (ContentNotFoundException) e;
//                     assertThat(ex.getDetails().get("id")).isEqualTo(contentId);
//                 });
//
//             then(playlistService).should(never()).addContent(any(), any());
//         }
//     }
//
//     @Nested
//     @DisplayName("deleteContentFromPlaylist()")
//     class DeleteContentFromPlaylistTest {
//
//         @Test
//         @DisplayName("소유자가 콘텐츠 삭제 성공")
//         void withOwner_deletesContentSuccess() {
//             // given
//             UserModel owner = UserModelFixture.create();
//             UUID playlistId = UUID.randomUUID();
//             UUID contentId = UUID.randomUUID();
//
//             PlaylistModel playlist = PlaylistModelFixture.builder(owner)
//                 .set("id", playlistId)
//                 .sample();
//
//             given(userService.getById(owner.getId())).willReturn(owner);
//             given(playlistService.getById(playlistId)).willReturn(playlist);
//             willDoNothing().given(playlistService).removeContent(playlistId, contentId);
//
//             // when & then
//             assertThatNoException()
//                 .isThrownBy(() -> playlistFacade.deleteContentFromPlaylist(owner.getId(),
//                     playlistId, contentId));
//
//             then(userService).should().getById(owner.getId());
//             then(playlistService).should().getById(playlistId);
//             then(playlistService).should().removeContent(playlistId, contentId);
//         }
//
//         @Test
//         @DisplayName("소유자가 아닌 사용자가 콘텐츠 삭제 시 PlaylistForbiddenException 발생")
//         void withNonOwner_throwsPlaylistForbiddenException() {
//             // given
//             UserModel owner = UserModelFixture.create();
//             UUID playlistId = UUID.randomUUID();
//             UUID nonOwnerId = UUID.randomUUID();
//             UUID contentId = UUID.randomUUID();
//
//             PlaylistModel playlist = PlaylistModelFixture.builder(owner)
//                 .set("id", playlistId)
//                 .sample();
//
//             given(userService.getById(nonOwnerId)).willReturn(
//                 UserModelFixture.builder().set("id", nonOwnerId).sample()
//             );
//             given(playlistService.getById(playlistId)).willReturn(playlist);
//
//             // when & then
//             assertThatThrownBy(
//                 () -> playlistFacade.deleteContentFromPlaylist(nonOwnerId, playlistId, contentId)
//             )
//                 .isInstanceOf(PlaylistForbiddenException.class)
//                 .satisfies(e -> {
//                     PlaylistForbiddenException ex = (PlaylistForbiddenException) e;
//                     assertThat(ex.getDetails().get("playlistId")).isEqualTo(playlistId);
//                     assertThat(ex.getDetails().get("requesterId")).isEqualTo(nonOwnerId);
//                     assertThat(ex.getDetails().get("ownerId")).isEqualTo(owner.getId());
//                 });
//
//             then(playlistService).should(never()).removeContent(any(UUID.class), any(UUID.class));
//         }
//     }
//
//     @Nested
//     @DisplayName("subscribePlaylist()")
//     class SubscribePlaylistTest {
//
//         @Test
//         @DisplayName("유효한 요청 시 구독 성공")
//         @SuppressWarnings("unchecked")
//         void withValidRequest_subscribesSuccess() {
//             // given
//             UserModel user = UserModelFixture.create();
//             UUID playlistId = UUID.randomUUID();
//             PlaylistModel playlistModel = PlaylistModelFixture.create();
//
//             given(userService.getById(user.getId())).willReturn(user);
//             given(playlistService.getById(playlistId)).willReturn(playlistModel);
//             willAnswer(invocation -> {
//                 invocation.getArgument(0, Consumer.class).accept(null);
//                 return null;
//             }).given(transactionTemplate).executeWithoutResult(any());
//             willDoNothing().given(playlistSubscriptionService).subscribe(playlistId, user.getId());
//
//             // when & then
//             assertThatNoException()
//                 .isThrownBy(() -> playlistFacade.subscribePlaylist(user.getId(), playlistId));
//
//             then(userService).should().getById(user.getId());
//             then(playlistService).should().getById(playlistId);
//             then(playlistSubscriptionService).should().subscribe(playlistId, user.getId());
//         }
//     }
//
//     @Nested
//     @DisplayName("unsubscribePlaylist()")
//     class UnsubscribePlaylistTest {
//
//         @Test
//         @DisplayName("유효한 요청 시 구독 취소 성공")
//         void withValidRequest_unsubscribesSuccess() {
//             // given
//             UserModel user = UserModelFixture.create();
//             UUID playlistId = UUID.randomUUID();
//             PlaylistModel playlistModel = PlaylistModelFixture.create();
//
//             given(userService.getById(user.getId())).willReturn(user);
//             given(playlistService.getById(playlistId)).willReturn(playlistModel);
//
//             // when & then
//             assertThatNoException()
//                 .isThrownBy(() -> playlistFacade.unsubscribePlaylist(user.getId(), playlistId));
//
//             then(userService).should().getById(user.getId());
//             then(playlistService).should().getById(playlistId);
//             then(playlistSubscriptionService).should().unsubscribe(playlistId, user.getId());
//         }
//     }
//
//     private static PlaylistResponse createPlaylistResponse(
//         UUID id,
//         long subscriberCount,
//         boolean subscribedByMe
//     ) {
//         return createPlaylistResponse(id, "제목", "설명", subscriberCount, subscribedByMe);
//     }
//
//     private static PlaylistResponse createPlaylistResponse(
//         UUID id,
//         String title,
//         String description,
//         long subscriberCount,
//         boolean subscribedByMe
//     ) {
//         return new PlaylistResponse(
//             id, null, title, description, null, subscriberCount, subscribedByMe,
//             Collections.emptyList()
//         );
//     }
// }
