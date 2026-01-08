package com.mopl.api.interfaces.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.api.application.user.UserFacade;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.domain.exception.user.SelfRoleChangeException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.user.UserModel;
import com.mopl.security.userdetails.MoplUserDetails;
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({ApiControllerAdvice.class, UserResponseMapper.class})
@DisplayName("UserController 역할 수정 테스트")
class UserRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserFacade userFacade;

    private MoplUserDetails mockAdminDetails;
    private UUID mockAdminId;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        mockAdminId = UUID.randomUUID();

        mockAdminDetails = mock(MoplUserDetails.class);
        given(mockAdminDetails.userId()).willReturn(mockAdminId);
        given(mockAdminDetails.getUsername()).willReturn(mockAdminId.toString());
        given(mockAdminDetails.getAuthorities())
            .willReturn(
                (Collection) Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
    }

    @Nested
    @DisplayName("PATCH /api/users/{userId}/role - 사용자 역할 수정")
    class UpdateRoleTest {

        @Test
        @DisplayName("유효한 요청 시 204 No Content 응답")
        void withValidRequest_returns204NoContent() throws Exception {
            // given
            UUID targetUserId = UUID.randomUUID();
            UserModel userModel = UserModelFixture.builder()
                .set("id", targetUserId)
                .sample();
            UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserModel.Role.ADMIN);

            given(userFacade.updateRole(eq(mockAdminId), any(UserRoleUpdateRequest.class),
                eq(targetUserId)))
                .willReturn(userModel);

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/role", targetUserId)
                .with(user(mockAdminDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

            then(userFacade).should()
                .updateRole(eq(mockAdminId), any(UserRoleUpdateRequest.class), eq(targetUserId));
        }

        @Test
        @DisplayName("role이 null인 경우 400 Bad Request 응답")
        void withNullRole_returns400BadRequest() throws Exception {
            // given
            UUID targetUserId = UUID.randomUUID();
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("role", null);

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/role", targetUserId)
                .with(user(mockAdminDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());

            then(userFacade).should(never()).updateRole(any(), any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 수정 시 404 Not Found 응답")
        void withNonExistingUserId_returns404NotFound() throws Exception {
            // given
            UUID targetUserId = UUID.randomUUID();
            UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserModel.Role.ADMIN);

            given(userFacade.updateRole(eq(mockAdminId), any(UserRoleUpdateRequest.class),
                eq(targetUserId)))
                .willThrow(UserNotFoundException.withId(targetUserId));

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/role", targetUserId)
                .with(user(mockAdminDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("유효하지 않은 role 값으로 요청 시 400 Bad Request 응답")
        void withInvalidRole_returns400BadRequest() throws Exception {
            // given
            UUID targetUserId = UUID.randomUUID();
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("role", "INVALID_ROLE");

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/role", targetUserId)
                .with(user(mockAdminDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());

            then(userFacade).should(never()).updateRole(any(), any(), any());
        }

        @Test
        @DisplayName("자기 자신의 역할 변경 시 400 Bad Request 응답")
        void withSelfRoleChange_returns400BadRequest() throws Exception {
            // given
            UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserModel.Role.USER);

            given(userFacade.updateRole(eq(mockAdminId), any(UserRoleUpdateRequest.class),
                eq(mockAdminId)))
                .willThrow(SelfRoleChangeException.withUserId(mockAdminId));

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/role", mockAdminId)
                .with(user(mockAdminDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }
}
