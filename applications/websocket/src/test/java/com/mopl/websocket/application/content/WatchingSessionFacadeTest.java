package com.mopl.websocket.application.content;

import com.mopl.domain.fixture.ContentModelFixture;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.fixture.WatchingSessionModelFixture;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.user.UserService;
import com.mopl.dto.watchingsession.WatchingSessionResponse;
import com.mopl.dto.watchingsession.WatchingSessionResponseMapper;
import com.mopl.websocket.interfaces.event.content.dto.WatchingSessionChangeResponse;
import com.mopl.websocket.interfaces.event.content.dto.WatchingSessionChangeType;
import com.mopl.websocket.messaging.WebSocketBroadcaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("WatchingSessionFacade 단위 테스트")
class WatchingSessionFacadeTest {

    @Mock
    private WatchingSessionRepository watchingSessionRepository;

    @Mock
    private UserService userService;

    @Mock
    private ContentService contentService;

    @Mock
    private WatchingSessionResponseMapper watchingSessionResponseMapper;

    @Mock
    private WebSocketBroadcaster webSocketBroadcaster;

    @InjectMocks
    private WatchingSessionFacade watchingSessionFacade;

    private UUID userId;
    private UUID contentId;
    private UserModel user;
    private ContentModel content;
    private WatchingSessionResponse mockResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        contentId = UUID.randomUUID();

        user = UserModelFixture.builder()
            .set("id", userId)
            .set("name", "TestUser")
            .set("profileImagePath", "profiles/test.jpg")
            .sample();

        content = ContentModelFixture.builder()
            .set("id", contentId)
            .set("title", "TestContent")
            .sample();

        mockResponse = new WatchingSessionResponse(
            userId,
            Instant.now(),
            null,
            null
        );
    }

    @Nested
    @DisplayName("joinSession()")
    class JoinSessionTest {

        @Test
        @DisplayName("기존 세션이 없으면 새로운 세션 생성")
        void withNoExistingSession_createsNewSession() {
            // given
            given(watchingSessionRepository.findByWatcherId(userId)).willReturn(Optional.empty());
            given(contentService.getById(contentId)).willReturn(content);
            given(userService.getById(userId)).willReturn(user);
            given(watchingSessionRepository.save(any(WatchingSessionModel.class)))
                .willAnswer(inv -> inv.getArgument(0));
            given(watchingSessionRepository.countByContentId(contentId)).willReturn(1L);
            given(watchingSessionResponseMapper.toResponse(any(WatchingSessionModel.class)))
                .willReturn(mockResponse);

            // when
            WatchingSessionChangeResponse result = watchingSessionFacade.joinSession(contentId, userId);

            // then
            assertThat(result.type()).isEqualTo(WatchingSessionChangeType.JOIN);
            assertThat(result.watcherCount()).isEqualTo(1L);

            then(watchingSessionRepository).should().save(any(WatchingSessionModel.class));
            then(contentService).should().getById(contentId);
            then(userService).should().getById(userId);
        }

        @Test
        @DisplayName("같은 콘텐츠에 기존 세션이 있으면 연결 카운트 증가")
        void withExistingSessionSameContent_incrementsConnectionCount() {
            // given
            WatchingSessionModel existingSession = WatchingSessionModelFixture.builder()
                .set("watcherId", userId)
                .set("contentId", contentId)
                .set("connectionCount", 1)
                .sample();

            given(watchingSessionRepository.findByWatcherId(userId)).willReturn(Optional.of(existingSession));
            given(watchingSessionRepository.save(any(WatchingSessionModel.class)))
                .willAnswer(inv -> inv.getArgument(0));
            given(watchingSessionRepository.countByContentId(contentId)).willReturn(1L);
            given(watchingSessionResponseMapper.toResponse(any(WatchingSessionModel.class)))
                .willReturn(mockResponse);

            // when
            WatchingSessionChangeResponse result = watchingSessionFacade.joinSession(contentId, userId);

            // then
            assertThat(result.type()).isEqualTo(WatchingSessionChangeType.JOIN);

            ArgumentCaptor<WatchingSessionModel> captor = ArgumentCaptor.forClass(WatchingSessionModel.class);
            then(watchingSessionRepository).should().save(captor.capture());
            assertThat(captor.getValue().getConnectionCount()).isEqualTo(2);

            then(contentService).shouldHaveNoInteractions();
            then(userService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("다른 콘텐츠에 기존 세션이 있으면 기존 세션 삭제 후 새로운 세션 생성")
        void withExistingSessionDifferentContent_deletesOldAndCreatesNew() {
            // given
            UUID oldContentId = UUID.randomUUID();
            WatchingSessionModel existingSession = WatchingSessionModelFixture.builder()
                .set("watcherId", userId)
                .set("contentId", oldContentId)
                .set("connectionCount", 1)
                .sample();

            given(watchingSessionRepository.findByWatcherId(userId)).willReturn(Optional.of(existingSession));
            given(watchingSessionResponseMapper.toResponse(existingSession)).willReturn(mockResponse);
            given(watchingSessionRepository.countByContentId(oldContentId)).willReturn(1L);
            given(contentService.getById(contentId)).willReturn(content);
            given(userService.getById(userId)).willReturn(user);
            given(watchingSessionRepository.save(any(WatchingSessionModel.class)))
                .willAnswer(inv -> inv.getArgument(0));
            given(watchingSessionRepository.countByContentId(contentId)).willReturn(1L);

            // when
            WatchingSessionChangeResponse result = watchingSessionFacade.joinSession(contentId, userId);

            // then
            assertThat(result.type()).isEqualTo(WatchingSessionChangeType.JOIN);

            then(watchingSessionRepository).should().delete(existingSession);
            then(webSocketBroadcaster).should().broadcast(
                eq("/sub/contents/" + oldContentId + "/watch"),
                any(WatchingSessionChangeResponse.class)
            );
            then(contentService).should().getById(contentId);
            then(userService).should().getById(userId);
        }

        @Test
        @DisplayName("다른 콘텐츠 떠날 때 LEAVE 메시지 브로드캐스트")
        void withDifferentContent_broadcastsLeaveMessage() {
            // given
            UUID oldContentId = UUID.randomUUID();
            WatchingSessionModel existingSession = WatchingSessionModelFixture.builder()
                .set("watcherId", userId)
                .set("contentId", oldContentId)
                .set("connectionCount", 1)
                .sample();

            given(watchingSessionRepository.findByWatcherId(userId)).willReturn(Optional.of(existingSession));
            given(watchingSessionResponseMapper.toResponse(existingSession)).willReturn(mockResponse);
            given(watchingSessionRepository.countByContentId(oldContentId)).willReturn(2L);
            given(contentService.getById(contentId)).willReturn(content);
            given(userService.getById(userId)).willReturn(user);
            given(watchingSessionRepository.save(any(WatchingSessionModel.class)))
                .willAnswer(inv -> inv.getArgument(0));
            given(watchingSessionRepository.countByContentId(contentId)).willReturn(1L);

            // when
            watchingSessionFacade.joinSession(contentId, userId);

            // then
            ArgumentCaptor<WatchingSessionChangeResponse> responseCaptor = ArgumentCaptor.forClass(WatchingSessionChangeResponse.class);
            then(webSocketBroadcaster).should().broadcast(
                eq("/sub/contents/" + oldContentId + "/watch"),
                responseCaptor.capture()
            );

            WatchingSessionChangeResponse leaveResponse = responseCaptor.getValue();
            assertThat(leaveResponse.type()).isEqualTo(WatchingSessionChangeType.LEAVE);
            assertThat(leaveResponse.watcherCount()).isEqualTo(1L); // 2 - 1 = 1
        }
    }

    @Nested
    @DisplayName("leaveSession()")
    class LeaveSessionTest {

        @Test
        @DisplayName("세션이 없으면 null 반환")
        void withNoSession_returnsNull() {
            // given
            given(watchingSessionRepository.findByWatcherId(userId)).willReturn(Optional.empty());

            // when
            WatchingSessionChangeResponse result = watchingSessionFacade.leaveSession(contentId, userId);

            // then
            assertThat(result).isNull();

            then(watchingSessionRepository).should(never()).delete(any());
            then(watchingSessionRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("다른 콘텐츠의 세션이면 null 반환")
        void withDifferentContentSession_returnsNull() {
            // given
            UUID otherContentId = UUID.randomUUID();
            WatchingSessionModel existingSession = WatchingSessionModelFixture.builder()
                .set("watcherId", userId)
                .set("contentId", otherContentId)
                .sample();

            given(watchingSessionRepository.findByWatcherId(userId)).willReturn(Optional.of(existingSession));

            // when
            WatchingSessionChangeResponse result = watchingSessionFacade.leaveSession(contentId, userId);

            // then
            assertThat(result).isNull();

            then(watchingSessionRepository).should(never()).delete(any());
            then(watchingSessionRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("연결이 하나 남으면 세션 삭제 후 LEAVE 응답 반환")
        void withOneConnection_deletesSessionAndReturnsLeave() {
            // given
            WatchingSessionModel existingSession = WatchingSessionModelFixture.builder()
                .set("watcherId", userId)
                .set("contentId", contentId)
                .set("connectionCount", 1)
                .sample();

            given(watchingSessionRepository.findByWatcherId(userId)).willReturn(Optional.of(existingSession));
            given(watchingSessionRepository.countByContentId(contentId)).willReturn(0L);
            given(watchingSessionResponseMapper.toResponse(existingSession)).willReturn(mockResponse);

            // when
            WatchingSessionChangeResponse result = watchingSessionFacade.leaveSession(contentId, userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo(WatchingSessionChangeType.LEAVE);
            assertThat(result.watcherCount()).isZero();

            then(watchingSessionRepository).should().delete(existingSession);
            then(watchingSessionRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("연결이 여러 개면 연결 카운트 감소 후 null 반환")
        void withMultipleConnections_decrementsCountAndReturnsNull() {
            // given
            WatchingSessionModel existingSession = WatchingSessionModelFixture.builder()
                .set("watcherId", userId)
                .set("contentId", contentId)
                .set("connectionCount", 3)
                .sample();

            given(watchingSessionRepository.findByWatcherId(userId)).willReturn(Optional.of(existingSession));
            given(watchingSessionRepository.save(any(WatchingSessionModel.class)))
                .willAnswer(inv -> inv.getArgument(0));

            // when
            WatchingSessionChangeResponse result = watchingSessionFacade.leaveSession(contentId, userId);

            // then
            assertThat(result).isNull();

            ArgumentCaptor<WatchingSessionModel> captor = ArgumentCaptor.forClass(WatchingSessionModel.class);
            then(watchingSessionRepository).should().save(captor.capture());
            assertThat(captor.getValue().getConnectionCount()).isEqualTo(2);

            then(watchingSessionRepository).should(never()).delete(any());
        }

        @Test
        @DisplayName("연결 카운트가 0이 되면 세션 삭제")
        void withConnectionCountBecomingZero_deletesSession() {
            // given
            WatchingSessionModel existingSession = WatchingSessionModelFixture.builder()
                .set("watcherId", userId)
                .set("contentId", contentId)
                .set("connectionCount", 1)
                .sample();

            given(watchingSessionRepository.findByWatcherId(userId)).willReturn(Optional.of(existingSession));
            given(watchingSessionRepository.countByContentId(contentId)).willReturn(5L);
            given(watchingSessionResponseMapper.toResponse(existingSession)).willReturn(mockResponse);

            // when
            WatchingSessionChangeResponse result = watchingSessionFacade.leaveSession(contentId, userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo(WatchingSessionChangeType.LEAVE);
            assertThat(result.watcherCount()).isEqualTo(5L);

            then(watchingSessionRepository).should().delete(existingSession);
        }
    }
}
