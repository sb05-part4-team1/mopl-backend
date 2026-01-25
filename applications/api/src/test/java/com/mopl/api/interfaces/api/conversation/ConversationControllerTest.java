package com.mopl.api.interfaces.api.conversation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.api.application.conversation.ConversationFacade;
import com.mopl.api.config.TestSecurityConfig;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.api.interfaces.api.conversation.dto.ConversationCreateRequest;
import com.mopl.api.interfaces.api.conversation.dto.ConversationResponse;
import com.mopl.api.interfaces.api.conversation.dto.DirectMessageResponse;
import com.mopl.api.interfaces.api.conversation.mapper.ConversationResponseMapper;
import com.mopl.api.interfaces.api.conversation.mapper.DirectMessageResponseMapper;
import com.mopl.api.interfaces.api.user.dto.UserSummary;
import com.mopl.api.interfaces.api.user.mapper.UserSummaryMapper;
import com.mopl.domain.exception.conversation.ConversationNotFoundException;
import com.mopl.domain.exception.conversation.ReadStatusNotFoundException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.security.userdetails.MoplUserDetails;
import com.mopl.storage.provider.StorageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ConversationController.class)
@Import({
    ApiControllerAdvice.class,
    ConversationResponseMapper.class,
    DirectMessageResponseMapper.class,
    UserSummaryMapper.class,
    TestSecurityConfig.class
})
@DisplayName("ConversationController 슬라이스 테스트")
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConversationFacade conversationFacade;

    @MockBean
    private StorageProvider storageProvider;

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
    @DisplayName("GET /api/conversations - 대화 목록 조회")
    class GetConversationsTest {

        @Test
        @DisplayName("유효한 요청 시 200 OK 응답")
        void withValidRequest_returns200OK() throws Exception {
            // given
            UUID conversationId = UUID.randomUUID();
            UUID withUserId = UUID.randomUUID();
            UserSummary withUser = new UserSummary(withUserId, "상대방", null);
            UserSummary sender = new UserSummary(userId, "나", null);
            DirectMessageResponse lastMessage = new DirectMessageResponse(
                UUID.randomUUID(),
                conversationId,
                Instant.now(),
                sender,
                withUser,
                "마지막 메시지"
            );
            ConversationResponse conversationResponse = new ConversationResponse(
                conversationId,
                withUser,
                lastMessage,
                true
            );
            CursorResponse<ConversationResponse> response = CursorResponse.of(
                List.of(conversationResponse),
                null,
                null,
                false,
                1L,
                "createdAt",
                SortDirection.DESCENDING
            );

            given(conversationFacade.getConversations(eq(userId), any())).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/conversations")
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(conversationId.toString()))
                .andExpect(jsonPath("$.data[0].with.userId").value(withUserId.toString()))
                .andExpect(jsonPath("$.data[0].hasUnread").value(true))
                .andExpect(jsonPath("$.data[0].lastMessage.content").value("마지막 메시지"));

            then(conversationFacade).should().getConversations(eq(userId), any());
        }

        @Test
        @DisplayName("빈 목록 시 200 OK 응답")
        void withEmptyList_returns200OK() throws Exception {
            // given
            CursorResponse<ConversationResponse> response = CursorResponse.empty(
                "createdAt",
                SortDirection.DESCENDING
            );

            given(conversationFacade.getConversations(eq(userId), any())).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/conversations")
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.hasNext").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/conversations/{conversationId} - 대화 상세 조회")
    class GetConversationTest {

        @Test
        @DisplayName("유효한 요청 시 200 OK 응답")
        void withValidRequest_returns200OK() throws Exception {
            // given
            UUID conversationId = UUID.randomUUID();
            UUID withUserId = UUID.randomUUID();
            UserSummary withUser = new UserSummary(withUserId, "상대방", null);
            ConversationResponse conversationResponse = new ConversationResponse(
                conversationId,
                withUser,
                null,
                false
            );

            given(conversationFacade.getConversation(userId, conversationId))
                .willReturn(conversationResponse);

            // when & then
            mockMvc.perform(get("/api/conversations/{conversationId}", conversationId)
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(conversationId.toString()))
                .andExpect(jsonPath("$.with.userId").value(withUserId.toString()))
                .andExpect(jsonPath("$.hasUnread").value(false));

            then(conversationFacade).should().getConversation(userId, conversationId);
        }

        @Test
        @DisplayName("존재하지 않는 대화 ID로 조회 시 404 Not Found 응답")
        void withNonExistingConversationId_returns404NotFound() throws Exception {
            // given
            UUID conversationId = UUID.randomUUID();

            given(conversationFacade.getConversation(userId, conversationId))
                .willThrow(ConversationNotFoundException.withId(conversationId));

            // when & then
            mockMvc.perform(get("/api/conversations/{conversationId}", conversationId)
                .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("참가자가 아닌 대화 조회 시 404 Not Found 응답")
        void withNonParticipant_returns404NotFound() throws Exception {
            // given
            UUID conversationId = UUID.randomUUID();

            given(conversationFacade.getConversation(userId, conversationId))
                .willThrow(ReadStatusNotFoundException.withParticipantIdAndConversationId(userId, conversationId));

            // when & then
            mockMvc.perform(get("/api/conversations/{conversationId}", conversationId)
                .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/conversations/with - 특정 사용자와의 대화 조회")
    class FindByWithTest {

        @Test
        @DisplayName("유효한 요청 시 200 OK 응답")
        void withValidRequest_returns200OK() throws Exception {
            // given
            UUID conversationId = UUID.randomUUID();
            UUID withUserId = UUID.randomUUID();
            UserSummary withUser = new UserSummary(withUserId, "상대방", null);
            ConversationResponse conversationResponse = new ConversationResponse(
                conversationId,
                withUser,
                null,
                false
            );

            given(conversationFacade.getConversationWith(userId, withUserId))
                .willReturn(conversationResponse);

            // when & then
            mockMvc.perform(get("/api/conversations/with")
                .param("userId", withUserId.toString())
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(conversationId.toString()))
                .andExpect(jsonPath("$.with.userId").value(withUserId.toString()));

            then(conversationFacade).should().getConversationWith(userId, withUserId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자와의 대화 조회 시 404 Not Found 응답")
        void withNonExistingUser_returns404NotFound() throws Exception {
            // given
            UUID withUserId = UUID.randomUUID();

            given(conversationFacade.getConversationWith(userId, withUserId))
                .willThrow(UserNotFoundException.withId(withUserId));

            // when & then
            mockMvc.perform(get("/api/conversations/with")
                .param("userId", withUserId.toString())
                .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/conversations/{conversationId}/direct-messages - DM 목록 조회")
    class GetDirectMessagesTest {

        @Test
        @DisplayName("유효한 요청 시 200 OK 응답")
        void withValidRequest_returns200OK() throws Exception {
            // given
            UUID conversationId = UUID.randomUUID();
            UUID messageId = UUID.randomUUID();
            UUID withUserId = UUID.randomUUID();
            UserSummary sender = new UserSummary(userId, "나", null);
            UserSummary receiver = new UserSummary(withUserId, "상대방", null);
            DirectMessageResponse messageResponse = new DirectMessageResponse(
                messageId,
                conversationId,
                Instant.now(),
                sender,
                receiver,
                "안녕하세요"
            );
            CursorResponse<DirectMessageResponse> response = CursorResponse.of(
                List.of(messageResponse),
                null,
                null,
                false,
                1L,
                "createdAt",
                SortDirection.DESCENDING
            );

            given(conversationFacade.getDirectMessages(eq(userId), eq(conversationId), any()))
                .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/conversations/{conversationId}/direct-messages", conversationId)
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(messageId.toString()))
                .andExpect(jsonPath("$.data[0].conversationId").value(conversationId.toString()))
                .andExpect(jsonPath("$.data[0].content").value("안녕하세요"));

            then(conversationFacade).should().getDirectMessages(eq(userId), eq(conversationId), any());
        }

        @Test
        @DisplayName("빈 목록 시 200 OK 응답")
        void withEmptyList_returns200OK() throws Exception {
            // given
            UUID conversationId = UUID.randomUUID();
            CursorResponse<DirectMessageResponse> response = CursorResponse.empty(
                "createdAt",
                SortDirection.DESCENDING
            );

            given(conversationFacade.getDirectMessages(eq(userId), eq(conversationId), any()))
                .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/conversations/{conversationId}/direct-messages", conversationId)
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.hasNext").value(false));
        }

        @Test
        @DisplayName("참가자가 아닌 대화의 DM 조회 시 404 Not Found 응답")
        void withNonParticipant_returns404NotFound() throws Exception {
            // given
            UUID conversationId = UUID.randomUUID();

            given(conversationFacade.getDirectMessages(eq(userId), eq(conversationId), any()))
                .willThrow(ReadStatusNotFoundException.withParticipantIdAndConversationId(userId, conversationId));

            // when & then
            mockMvc.perform(get("/api/conversations/{conversationId}/direct-messages", conversationId)
                .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("존재하지 않는 대화의 DM 조회 시 404 Not Found 응답")
        void withNonExistingConversation_returns404NotFound() throws Exception {
            // given
            UUID conversationId = UUID.randomUUID();

            given(conversationFacade.getDirectMessages(eq(userId), eq(conversationId), any()))
                .willThrow(ConversationNotFoundException.withId(conversationId));

            // when & then
            mockMvc.perform(get("/api/conversations/{conversationId}/direct-messages", conversationId)
                .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/conversations - 대화 생성")
    class CreateConversationTest {

        @Test
        @DisplayName("유효한 요청 시 201 Created 응답")
        void withValidRequest_returns201Created() throws Exception {
            // given
            UUID withUserId = UUID.randomUUID();
            ConversationCreateRequest request = new ConversationCreateRequest(withUserId);

            UUID conversationId = UUID.randomUUID();
            UserSummary withUser = new UserSummary(withUserId, "상대방", null);
            ConversationResponse conversationResponse = new ConversationResponse(
                conversationId,
                withUser,
                null,
                false
            );

            given(conversationFacade.createConversation(eq(userId), any(ConversationCreateRequest.class)))
                .willReturn(conversationResponse);

            // when & then
            mockMvc.perform(post("/api/conversations")
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(conversationId.toString()))
                .andExpect(jsonPath("$.with.userId").value(withUserId.toString()));

            then(conversationFacade).should().createConversation(eq(userId), any(ConversationCreateRequest.class));
        }

        @Test
        @DisplayName("withUserId가 null인 경우 400 Bad Request 응답")
        void withNullWithUserId_returns400BadRequest() throws Exception {
            // given
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("withUserId", null);

            // when & then
            mockMvc.perform(post("/api/conversations")
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());

            then(conversationFacade).should(never()).createConversation(any(), any());
        }
    }

    @Nested
    @DisplayName("POST /api/conversations/{conversationId}/direct-messages/{directMessageId}/read - 메시지 읽음 처리")
    class MarkAsReadTest {

        @Test
        @DisplayName("유효한 요청 시 204 No Content 응답")
        void withValidRequest_returns204NoContent() throws Exception {
            // given
            UUID conversationId = UUID.randomUUID();
            UUID directMessageId = UUID.randomUUID();

            willDoNothing().given(conversationFacade)
                .markAsRead(userId, conversationId);

            // when & then
            mockMvc.perform(post("/api/conversations/{conversationId}/direct-messages/{directMessageId}/read",
                conversationId, directMessageId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNoContent());

            then(conversationFacade).should().markAsRead(userId, conversationId);
        }

        @Test
        @DisplayName("존재하지 않는 대화에서 읽음 처리 시 404 Not Found 응답")
        void withNonExistingConversation_returns404NotFound() throws Exception {
            // given
            UUID conversationId = UUID.randomUUID();
            UUID directMessageId = UUID.randomUUID();

            willThrow(ConversationNotFoundException.withId(conversationId))
                .given(conversationFacade).markAsRead(userId, conversationId);

            // when & then
            mockMvc.perform(post("/api/conversations/{conversationId}/direct-messages/{directMessageId}/read",
                conversationId, directMessageId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("참가자가 아닌 대화의 메시지 읽음 처리 시 404 Not Found 응답")
        void withNonParticipant_returns404NotFound() throws Exception {
            // given
            UUID conversationId = UUID.randomUUID();
            UUID directMessageId = UUID.randomUUID();

            willThrow(ReadStatusNotFoundException.withParticipantIdAndConversationId(userId, conversationId))
                .given(conversationFacade).markAsRead(userId, conversationId);

            // when & then
            mockMvc.perform(post("/api/conversations/{conversationId}/direct-messages/{directMessageId}/read",
                conversationId, directMessageId)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isNotFound());
        }
    }
}
