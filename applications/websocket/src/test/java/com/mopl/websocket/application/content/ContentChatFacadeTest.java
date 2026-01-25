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
import com.mopl.dto.user.UserSummary;
import com.mopl.dto.user.UserSummaryMapper;
import com.mopl.dto.watchingsession.WatchingSessionResponse;
import com.mopl.dto.watchingsession.WatchingSessionResponseMapper;
import com.mopl.websocket.interfaces.api.content.dto.ContentChatResponse;
import com.mopl.websocket.interfaces.event.content.dto.WatchingSessionChangeResponse;
import com.mopl.websocket.interfaces.event.content.dto.WatchingSessionChangeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

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
@DisplayName("ContentChatFacade 단위 테스트")
class ContentChatFacadeTest {

    @Mock
    private UserService userService;

    @Mock
    private ContentService contentService;

    @Mock
    private WatchingSessionRepository watchingSessionRepository;

    @Mock
    private UserSummaryMapper userSummaryMapper;

    @Mock
    private WatchingSessionResponseMapper watchingSessionResponseMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ContentChatFacade contentChatFacade;

    private UUID userId;
    private UUID contentId;
    private UserModel user;
    private ContentModel content;
    private UserSummary userSummary;
    private WatchingSessionResponse mockWatchingSessionResponse;

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

        userSummary = new UserSummary(userId, "TestUser", "http://example.com/profiles/test.jpg");

        mockWatchingSessionResponse = new WatchingSessionResponse(
            UUID.randomUUID(),
            Instant.now(),
            null,
            null
        );
    }

    @Nested
    @DisplayName("sendChatMessage()")
    class SendChatMessageTest {

        @Test
        @DisplayName("같은 콘텐츠에 기존 세션이 있으면 메시지만 전송")
        void withExistingSessionSameContent_sendsChatMessage() {
            // given
            String message = "안녕하세요!";
            WatchingSessionModel existingSession = WatchingSessionModelFixture.builder()
                .set("watcherId", userId)
                .set("contentId", contentId)
                .sample();

            given(watchingSessionRepository.findByWatcherId(userId)).willReturn(Optional.of(existingSession));
            given(userService.getById(userId)).willReturn(user);
            given(userSummaryMapper.toSummary(user)).willReturn(userSummary);

            // when
            ContentChatResponse result = contentChatFacade.sendChatMessage(userId, contentId, message);

            // then
            assertThat(result.sender()).isEqualTo(userSummary);
            assertThat(result.content()).isEqualTo(message);

            then(watchingSessionRepository).should(never()).save(any());
            then(watchingSessionRepository).should(never()).delete(any());
            then(messagingTemplate).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("세션이 없으면 새로운 세션 생성 후 메시지 전송")
        void withNoExistingSession_createsSessionAndSendsChatMessage() {
            // given
            String message = "첫 메시지입니다";

            given(watchingSessionRepository.findByWatcherId(userId)).willReturn(Optional.empty());
            given(contentService.getById(contentId)).willReturn(content);
            given(userService.getById(userId)).willReturn(user);
            given(watchingSessionRepository.save(any(WatchingSessionModel.class)))
                .willAnswer(inv -> inv.getArgument(0));
            given(watchingSessionRepository.countByContentId(contentId)).willReturn(1L);
            given(watchingSessionResponseMapper.toDto(any(WatchingSessionModel.class)))
                .willReturn(mockWatchingSessionResponse);
            given(userSummaryMapper.toSummary(user)).willReturn(userSummary);

            // when
            ContentChatResponse result = contentChatFacade.sendChatMessage(userId, contentId, message);

            // then
            assertThat(result.sender()).isEqualTo(userSummary);
            assertThat(result.content()).isEqualTo(message);

            then(watchingSessionRepository).should().save(any(WatchingSessionModel.class));
            then(messagingTemplate).should().convertAndSend(
                eq("/sub/contents/" + contentId + "/watch"),
                any(WatchingSessionChangeResponse.class)
            );
        }

        @Test
        @DisplayName("다른 콘텐츠에 기존 세션이 있으면 기존 세션 삭제 후 새 세션 생성")
        void withExistingSessionDifferentContent_deletesOldAndCreatesNewSession() {
            // given
            String message = "다른 콘텐츠에서 왔어요";
            UUID oldContentId = UUID.randomUUID();
            WatchingSessionModel existingSession = WatchingSessionModelFixture.builder()
                .set("watcherId", userId)
                .set("contentId", oldContentId)
                .sample();

            given(watchingSessionRepository.findByWatcherId(userId)).willReturn(Optional.of(existingSession));
            given(watchingSessionRepository.countByContentId(oldContentId)).willReturn(0L);
            given(watchingSessionResponseMapper.toDto(existingSession)).willReturn(mockWatchingSessionResponse);
            given(contentService.getById(contentId)).willReturn(content);
            given(userService.getById(userId)).willReturn(user);
            given(watchingSessionRepository.save(any(WatchingSessionModel.class)))
                .willAnswer(inv -> inv.getArgument(0));
            given(watchingSessionRepository.countByContentId(contentId)).willReturn(1L);
            given(userSummaryMapper.toSummary(user)).willReturn(userSummary);

            // when
            ContentChatResponse result = contentChatFacade.sendChatMessage(userId, contentId, message);

            // then
            assertThat(result.sender()).isEqualTo(userSummary);
            assertThat(result.content()).isEqualTo(message);

            then(watchingSessionRepository).should().delete(existingSession);
            then(watchingSessionRepository).should().save(any(WatchingSessionModel.class));
        }

        @Test
        @DisplayName("다른 콘텐츠 떠날 때 LEAVE 메시지 브로드캐스트")
        void withDifferentContent_broadcastsLeaveMessage() {
            // given
            String message = "메시지";
            UUID oldContentId = UUID.randomUUID();
            WatchingSessionModel existingSession = WatchingSessionModelFixture.builder()
                .set("watcherId", userId)
                .set("contentId", oldContentId)
                .sample();

            given(watchingSessionRepository.findByWatcherId(userId)).willReturn(Optional.of(existingSession));
            given(watchingSessionRepository.countByContentId(oldContentId)).willReturn(3L);
            given(watchingSessionResponseMapper.toDto(existingSession)).willReturn(mockWatchingSessionResponse);
            given(contentService.getById(contentId)).willReturn(content);
            given(userService.getById(userId)).willReturn(user);
            given(watchingSessionRepository.save(any(WatchingSessionModel.class)))
                .willAnswer(inv -> inv.getArgument(0));
            given(watchingSessionRepository.countByContentId(contentId)).willReturn(1L);
            given(userSummaryMapper.toSummary(user)).willReturn(userSummary);

            // when
            contentChatFacade.sendChatMessage(userId, contentId, message);

            // then
            ArgumentCaptor<WatchingSessionChangeResponse> responseCaptor = ArgumentCaptor.forClass(WatchingSessionChangeResponse.class);
            then(messagingTemplate).should().convertAndSend(
                eq("/sub/contents/" + oldContentId + "/watch"),
                responseCaptor.capture()
            );

            WatchingSessionChangeResponse leaveResponse = responseCaptor.getValue();
            assertThat(leaveResponse.type()).isEqualTo(WatchingSessionChangeType.LEAVE);
            assertThat(leaveResponse.watcherCount()).isEqualTo(2L); // 3 - 1 = 2
        }

        @Test
        @DisplayName("새 세션 생성 시 JOIN 메시지 브로드캐스트")
        void withNewSession_broadcastsJoinMessage() {
            // given
            String message = "새로 참여했어요";

            given(watchingSessionRepository.findByWatcherId(userId)).willReturn(Optional.empty());
            given(contentService.getById(contentId)).willReturn(content);
            given(userService.getById(userId)).willReturn(user);
            given(watchingSessionRepository.save(any(WatchingSessionModel.class)))
                .willAnswer(inv -> inv.getArgument(0));
            given(watchingSessionRepository.countByContentId(contentId)).willReturn(5L);
            given(watchingSessionResponseMapper.toDto(any(WatchingSessionModel.class)))
                .willReturn(mockWatchingSessionResponse);
            given(userSummaryMapper.toSummary(user)).willReturn(userSummary);

            // when
            contentChatFacade.sendChatMessage(userId, contentId, message);

            // then
            ArgumentCaptor<WatchingSessionChangeResponse> responseCaptor = ArgumentCaptor.forClass(WatchingSessionChangeResponse.class);
            then(messagingTemplate).should().convertAndSend(
                eq("/sub/contents/" + contentId + "/watch"),
                responseCaptor.capture()
            );

            WatchingSessionChangeResponse joinResponse = responseCaptor.getValue();
            assertThat(joinResponse.type()).isEqualTo(WatchingSessionChangeType.JOIN);
            assertThat(joinResponse.watcherCount()).isEqualTo(5L);
        }
    }
}
