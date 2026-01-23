package com.mopl.api.interfaces.api.notification;

import com.mopl.api.application.notification.NotificationFacade;
import com.mopl.api.config.TestSecurityConfig;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.api.interfaces.api.notification.dto.NotificationResponse;
import com.mopl.api.interfaces.api.notification.mapper.NotificationResponseMapper;
import com.mopl.domain.exception.notification.NotificationNotFoundException;
import com.mopl.domain.exception.notification.NotificationForbiddenException;
import com.mopl.domain.fixture.NotificationModelFixture;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.notification.NotificationQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.security.userdetails.MoplUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import({ApiControllerAdvice.class, NotificationResponseMapper.class, TestSecurityConfig.class})
@DisplayName("NotificationController 슬라이스 테스트")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationResponseMapper notificationResponseMapper;

    @MockBean
    private NotificationFacade notificationFacade;

    private MoplUserDetails mockUserDetails;
    private UUID userId;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        userId = UUID.randomUUID();

        mockUserDetails = mock(MoplUserDetails.class);
        given(mockUserDetails.userId()).willReturn(userId);
        given(mockUserDetails.getUsername()).willReturn(userId.toString());
        given(mockUserDetails.getAuthorities()).willReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Nested
    @DisplayName("GET /api/notifications - 알림 목록 조회")
    class GetNotificationsTest {

        @Test
        @DisplayName("유효한 요청 시 200 OK 응답과 알림 목록 반환")
        void withValidRequest_returns200OKWithNotificationList() throws Exception {
            // given
            UserModel receiver = UserModelFixture.builder()
                .set("id", userId)
                .sample();
            NotificationModel notification1 = NotificationModelFixture.builder()
                .set("receiver", receiver)
                .set("title", "알림1")
                .set("content", "알림 내용1")
                .set("level", NotificationModel.NotificationLevel.INFO)
                .sample();
            NotificationModel notification2 = NotificationModelFixture.builder()
                .set("receiver", receiver)
                .set("title", "알림2")
                .set("content", "알림 내용2")
                .set("level", NotificationModel.NotificationLevel.WARNING)
                .sample();

            NotificationResponse response1 = notificationResponseMapper.toResponse(notification1);
            NotificationResponse response2 = notificationResponseMapper.toResponse(notification2);

            CursorResponse<NotificationResponse> cursorResponse = CursorResponse.of(
                List.of(response1, response2),
                notification2.getCreatedAt().toString(),
                notification2.getId(),
                true,
                10,
                "createdAt",
                SortDirection.ASCENDING
            );

            given(notificationFacade.getNotifications(eq(userId), any(
                NotificationQueryRequest.class)))
                .willReturn(cursorResponse);

            // when & then
            mockMvc.perform(get("/api/notifications")
                .with(user(mockUserDetails))
                .param("limit", "10")
                .param("sortDirection", "ASCENDING")
                .param("sortBy", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("알림1"))
                .andExpect(jsonPath("$.data[0].level").value("INFO"))
                .andExpect(jsonPath("$.data[1].title").value("알림2"))
                .andExpect(jsonPath("$.data[1].level").value("WARNING"))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.totalCount").value(10))
                .andExpect(jsonPath("$.sortBy").value("createdAt"))
                .andExpect(jsonPath("$.sortDirection").value("ASCENDING"));

            then(notificationFacade).should().getNotifications(eq(userId), any(
                NotificationQueryRequest.class));
        }

        @Test
        @DisplayName("커서 기반 페이지네이션 요청 처리")
        void withCursorParams_handlesPagination() throws Exception {
            // given
            UUID idAfter = UUID.randomUUID();
            UserModel receiver = UserModelFixture.builder()
                .set("id", userId)
                .sample();
            NotificationModel notification = NotificationModelFixture.builder()
                .set("receiver", receiver)
                .sample();
            NotificationResponse response = notificationResponseMapper.toResponse(notification);

            CursorResponse<NotificationResponse> cursorResponse = CursorResponse.of(
                List.of(response),
                null,
                null,
                false,
                5,
                "createdAt",
                SortDirection.DESCENDING
            );

            given(notificationFacade.getNotifications(eq(userId), any(
                NotificationQueryRequest.class)))
                .willReturn(cursorResponse);

            // when & then
            mockMvc.perform(get("/api/notifications")
                .with(user(mockUserDetails))
                .param("cursor", "2025-01-01T00:00:00Z")
                .param("idAfter", idAfter.toString())
                .param("limit", "10")
                .param("sortDirection", "DESCENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());

            then(notificationFacade).should().getNotifications(eq(userId), any(
                NotificationQueryRequest.class));
        }

        @Test
        @DisplayName("빈 결과 시 빈 목록 반환")
        void withNoResults_returnsEmptyList() throws Exception {
            // given
            CursorResponse<NotificationResponse> emptyResponse = CursorResponse.empty(
                "createdAt",
                SortDirection.ASCENDING
            );

            given(notificationFacade.getNotifications(eq(userId), any(
                NotificationQueryRequest.class)))
                .willReturn(emptyResponse);

            // when & then
            mockMvc.perform(get("/api/notifications")
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.totalCount").value(0));

            then(notificationFacade).should().getNotifications(eq(userId), any(
                NotificationQueryRequest.class));
        }

    }

    @Nested
    @DisplayName("DELETE /api/notifications/{notificationId} - 알림 읽음 처리")
    class ReadNotificationTest {

        @Test
        @DisplayName("유효한 요청 시 204 No Content 응답")
        void withValidRequest_returns204NoContent() throws Exception {
            // given
            UUID notificationId = UUID.randomUUID();

            willDoNothing().given(notificationFacade).readNotification(userId, notificationId);

            // when & then
            mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
                .with(user(mockUserDetails)))
                .andExpect(status().isNoContent());

            then(notificationFacade).should().readNotification(userId, notificationId);
        }

        @Test
        @DisplayName("존재하지 않는 알림 ID로 요청 시 404 Not Found 응답")
        void withNonExistingNotificationId_returns404NotFound() throws Exception {
            // given
            UUID notificationId = UUID.randomUUID();

            willThrow(NotificationNotFoundException.withId(notificationId))
                .given(notificationFacade).readNotification(userId, notificationId);

            // when & then
            mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
                .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("다른 사용자의 알림에 접근 시 403 Forbidden 응답")
        void withOtherUserNotification_returns403Forbidden() throws Exception {
            // given
            UUID notificationId = UUID.randomUUID();

            willThrow(NotificationForbiddenException.withNotificationIdAndUserId(notificationId, userId))
                .given(notificationFacade).readNotification(userId, notificationId);

            // when & then
            mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
                .with(user(mockUserDetails)))
                .andExpect(status().isForbidden());
        }
    }
}
