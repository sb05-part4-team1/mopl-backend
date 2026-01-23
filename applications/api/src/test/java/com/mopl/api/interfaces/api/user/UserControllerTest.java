package com.mopl.api.interfaces.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.api.application.user.UserFacade;
import com.mopl.api.config.TestSecurityConfig;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.api.interfaces.api.user.dto.ChangePasswordRequest;
import com.mopl.api.interfaces.api.user.dto.UserCreateRequest;
import com.mopl.api.interfaces.api.user.dto.UserLockUpdateRequest;
import com.mopl.api.interfaces.api.user.dto.UserRoleUpdateRequest;
import com.mopl.api.interfaces.api.user.dto.UserUpdateRequest;
import com.mopl.api.interfaces.api.user.mapper.UserResponseMapper;
import com.mopl.domain.exception.user.DuplicateEmailException;
import com.mopl.domain.exception.user.InvalidUserDataException;
import com.mopl.domain.exception.user.SelfLockChangeException;
import com.mopl.domain.exception.user.SelfRoleChangeException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.security.userdetails.MoplUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({
    ApiControllerAdvice.class,
    UserResponseMapper.class,
    TestSecurityConfig.class
})
@DisplayName("UserController 슬라이스 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserFacade userFacade;

    private MoplUserDetails mockAdminDetails;
    private MoplUserDetails mockUserDetails;
    private UUID adminId;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockAdminDetails = mock(MoplUserDetails.class);
        given(mockAdminDetails.userId()).willReturn(adminId);
        given(mockAdminDetails.getUsername()).willReturn(adminId.toString());
        given(mockAdminDetails.getAuthorities()).willReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        mockUserDetails = mock(MoplUserDetails.class);
        given(mockUserDetails.userId()).willReturn(userId);
        given(mockUserDetails.getUsername()).willReturn(userId.toString());
        given(mockUserDetails.getAuthorities()).willReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Nested
    @DisplayName("POST /api/users - 회원가입")
    class SignUpTest {

        @Test
        @DisplayName("유효한 요청 시 201 Created 응답")
        void withValidRequest_returns201Created() throws Exception {
            // given
            String email = "test@example.com";
            String name = "test";
            String password = "P@ssw0rd!";

            UserCreateRequest request = new UserCreateRequest(email, name, password);

            UserModel userModel = UserModelFixture.builder()
                .set("email", email)
                .set("name", name)
                .sample();

            given(userFacade.signUp(any(UserCreateRequest.class))).willReturn(userModel);

            // when & then
            mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userModel.getId().toString()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.locked").value(false));

            then(userFacade).should().signUp(any(UserCreateRequest.class));
        }

        static Stream<Arguments> invalidRequestProvider() {
            return Stream.of(
                Arguments.of("이메일이 비어있음", "", "test", "P@ssw0rd!"),
                Arguments.of("이메일이 null", null, "test", "P@ssw0rd!"),
                Arguments.of("이메일이 255자 초과", "a".repeat(256), "test", "P@ssw0rd!"),
                Arguments.of("이름이 비어있음", "test@example.com", "", "P@ssw0rd!"),
                Arguments.of("이름이 null", "test@example.com", null, "P@ssw0rd!"),
                Arguments.of("이름이 50자 초과", "test@example.com", "a".repeat(51), "P@ssw0rd!"),
                Arguments.of("비밀번호가 비어있음", "test@example.com", "test", ""),
                Arguments.of("비밀번호가 null", "test@example.com", "test", null),
                Arguments.of("비밀번호가 50자 초과", "test@example.com", "test", "a".repeat(51))
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidRequestProvider")
        @DisplayName("유효하지 않은 요청 시 400 Bad Request 응답")
        void withInvalidRequest_returns400BadRequest(
            String description,
            String email,
            String name,
            String password
        ) throws Exception {
            // given
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("name", name);
            requestBody.put("password", password);

            // when & then
            mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());

            then(userFacade).should(never()).signUp(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("중복 이메일로 가입 시 409 Conflict 응답")
        void withDuplicateEmail_returns409Conflict() throws Exception {
            // given
            String email = "duplicate@example.com";
            UserCreateRequest request = new UserCreateRequest(email, "test", "P@ssw0rd!");

            given(userFacade.signUp(any(UserCreateRequest.class)))
                .willThrow(DuplicateEmailException.withEmail(email));

            // when & then
            mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("유효하지 않은 사용자 데이터로 가입 시 400 Bad Request 응답")
        void withInvalidUserData_returns400BadRequest() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest("test@example.com", "test",
                "P@ssw0rd!");

            given(userFacade.signUp(any(UserCreateRequest.class)))
                .willThrow(InvalidUserDataException.withDetailMessage("이메일은 비어있을 수 없습니다."));

            // when & then
            mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{userId} - 사용자 상세 조회")
    class GetUserTest {

        @Test
        @DisplayName("유효한 사용자 ID로 조회 시 200 OK 응답")
        void withValidUserId_returns200OK() throws Exception {
            // given
            UserModel userModel = UserModelFixture.create();

            given(userFacade.getUser(userModel.getId())).willReturn(userModel);

            // when & then
            mockMvc.perform(get("/api/users/{userId}", userModel.getId())
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userModel.getId().toString()))
                .andExpect(jsonPath("$.email").value(userModel.getEmail()))
                .andExpect(jsonPath("$.name").value(userModel.getName()))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.locked").value(false));

            then(userFacade).should().getUser(userModel.getId());
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회 시 404 Not Found 응답")
        void withNonExistingUserId_returns404NotFound() throws Exception {
            // given
            UUID nonExistingUserId = UUID.randomUUID();

            given(userFacade.getUser(nonExistingUserId))
                .willThrow(UserNotFoundException.withId(nonExistingUserId));

            // when & then
            mockMvc.perform(get("/api/users/{userId}", nonExistingUserId)
                .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{userId} - 프로필 수정")
    class UpdateProfileTest {

        @Test
        @DisplayName("유효한 프로필 이미지로 수정 시 200 OK 응답")
        void withValidProfileImage_returns200OK() throws Exception {
            // given
            String profileImageUrl = "http://localhost/api/v1/files/display?path=users/test.png";
            UserModel userModel = UserModelFixture.builder()
                .set("profileImageUrl", profileImageUrl)
                .sample();

            MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
            );

            given(userFacade.updateProfile(
                eq(userModel.getId()),
                isNull(),
                any(MultipartFile.class))
            ).willReturn(userModel);

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}", userModel.getId())
                .file(image)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userModel.getId().toString()))
                .andExpect(jsonPath("$.profileImageUrl").value(profileImageUrl));

            then(userFacade).should().updateProfile(
                eq(userModel.getId()),
                isNull(),
                any(MultipartFile.class));
        }

        @Test
        @DisplayName("유효한 이름으로 수정 시 200 OK 응답")
        void withValidName_returns200OK() throws Exception {
            // given
            String newName = "newName";
            UserModel userModel = UserModelFixture.builder()
                .set("name", newName)
                .sample();

            UserUpdateRequest request = new UserUpdateRequest(newName);
            MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
            );

            given(userFacade.updateProfile(
                eq(userModel.getId()),
                any(UserUpdateRequest.class),
                isNull())
            ).willReturn(userModel);

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}", userModel.getId())
                .file(requestPart)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userModel.getId().toString()))
                .andExpect(jsonPath("$.name").value(newName));

            then(userFacade).should().updateProfile(
                eq(userModel.getId()),
                any(UserUpdateRequest.class),
                isNull());
        }

        @Test
        @DisplayName("이름과 이미지 함께 수정 시 200 OK 응답")
        void withNameAndImage_returns200OK() throws Exception {
            // given
            String newName = "newName";
            String profileImageUrl = "http://localhost/api/v1/files/display?path=users/test.png";
            UserModel userModel = UserModelFixture.builder()
                .set("name", newName)
                .set("profileImageUrl", profileImageUrl)
                .sample();

            UserUpdateRequest request = new UserUpdateRequest(newName);
            MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
            );

            MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
            );

            given(userFacade.updateProfile(
                eq(userModel.getId()),
                any(UserUpdateRequest.class),
                any(MultipartFile.class))
            ).willReturn(userModel);

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}", userModel.getId())
                .file(requestPart)
                .file(image)
                .with(user(mockUserDetails))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userModel.getId().toString()))
                .andExpect(jsonPath("$.name").value(newName))
                .andExpect(jsonPath("$.profileImageUrl").value(profileImageUrl));

            then(userFacade).should().updateProfile(
                eq(userModel.getId()),
                any(UserUpdateRequest.class),
                any(MultipartFile.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 수정 시 404 Not Found 응답")
        void withNonExistingUserId_returns404NotFound() throws Exception {
            // given
            UUID nonExistingUserId = UUID.randomUUID();

            MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
            );

            given(userFacade.updateProfile(
                eq(nonExistingUserId),
                isNull(),
                any(MultipartFile.class))
            ).willThrow(UserNotFoundException.withId(nonExistingUserId));

            // when & then
            mockMvc.perform(
                multipart(HttpMethod.PATCH, "/api/users/{userId}", nonExistingUserId)
                    .file(image)
                    .with(user(mockUserDetails))
                    .with(csrf())
            ).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/users - 사용자 목록 조회")
    class GetUsersTest {

        @Test
        @DisplayName("유효한 요청 시 200 OK 응답과 사용자 목록 반환")
        void withValidRequest_returns200OKWithUserList() throws Exception {
            // given
            UserModel user1 = UserModelFixture.builder()
                .set("email", "user1@example.com")
                .set("name", "User1")
                .sample();
            UserModel user2 = UserModelFixture.builder()
                .set("email", "user2@example.com")
                .set("name", "User2")
                .sample();

            CursorResponse<UserModel> cursorResponse = CursorResponse.of(
                List.of(user1, user2),
                "User2",
                user2.getId(),
                true,
                10,
                "name",
                SortDirection.ASCENDING
            );

            given(userFacade.getUsers(any(UserQueryRequest.class))).willReturn(cursorResponse);

            // when & then
            mockMvc.perform(get("/api/users")
                .with(user(mockAdminDetails))
                .param("limit", "10")
                .param("sortDirection", "ASCENDING")
                .param("sortBy", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$.data[1].email").value("user2@example.com"))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value("User2"))
                .andExpect(jsonPath("$.totalCount").value(10))
                .andExpect(jsonPath("$.sortBy").value("name"))
                .andExpect(jsonPath("$.sortDirection").value("ASCENDING"));

            then(userFacade).should().getUsers(any(UserQueryRequest.class));
        }

        @Test
        @DisplayName("필터 파라미터가 적용된 요청 처리")
        void withFilterParams_appliesFilters() throws Exception {
            // given
            CursorResponse<UserModel> emptyResponse = CursorResponse.empty("name",
                SortDirection.ASCENDING);

            given(userFacade.getUsers(any(UserQueryRequest.class))).willReturn(emptyResponse);

            // when & then
            mockMvc.perform(get("/api/users")
                .with(user(mockAdminDetails))
                .param("emailLike", "admin")
                .param("roleEqual", "ADMIN")
                .param("isLocked", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.hasNext").value(false));

            then(userFacade).should().getUsers(any(UserQueryRequest.class));
        }

        @Test
        @DisplayName("커서 기반 페이지네이션 요청 처리")
        void withCursorParams_handlesPagination() throws Exception {
            // given
            UUID idAfter = UUID.randomUUID();
            UserModel userModel = UserModelFixture.create();

            CursorResponse<UserModel> cursorResponse = CursorResponse.of(
                List.of(userModel),
                null,
                null,
                false,
                5,
                "name",
                SortDirection.ASCENDING
            );

            given(userFacade.getUsers(any(UserQueryRequest.class))).willReturn(cursorResponse);

            // when & then
            mockMvc.perform(get("/api/users")
                .with(user(mockAdminDetails))
                .param("cursor", "PreviousUser")
                .param("idAfter", idAfter.toString())
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());

            then(userFacade).should().getUsers(any(UserQueryRequest.class));
        }

        @Test
        @DisplayName("빈 결과 시 빈 목록 반환")
        void withNoResults_returnsEmptyList() throws Exception {
            // given
            CursorResponse<UserModel> emptyResponse = CursorResponse.empty("name",
                SortDirection.DESCENDING);

            given(userFacade.getUsers(any(UserQueryRequest.class))).willReturn(emptyResponse);

            // when & then
            mockMvc.perform(get("/api/users")
                .with(user(mockAdminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.totalCount").value(0));

            then(userFacade).should().getUsers(any(UserQueryRequest.class));
        }
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

            given(userFacade.updateRole(
                eq(adminId),
                any(UserRoleUpdateRequest.class),
                eq(targetUserId)
            )).willReturn(userModel);

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/role", targetUserId)
                .with(user(mockAdminDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

            then(userFacade).should().updateRole(
                eq(adminId),
                any(UserRoleUpdateRequest.class),
                eq(targetUserId)
            );
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

            given(userFacade.updateRole(
                eq(adminId),
                any(UserRoleUpdateRequest.class),
                eq(targetUserId)
            )).willThrow(UserNotFoundException.withId(targetUserId));

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

            given(userFacade.updateRole(
                eq(adminId),
                any(UserRoleUpdateRequest.class),
                eq(adminId)
            )).willThrow(SelfRoleChangeException.withUserId(adminId));

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/role", adminId)
                .with(user(mockAdminDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{userId}/locked - 사용자 잠금 상태 수정")
    class UpdateLockedTest {

        @Test
        @DisplayName("유효한 요청 시 204 No Content 응답")
        void withValidRequest_returns204NoContent() throws Exception {
            // given
            UUID targetUserId = UUID.randomUUID();
            UserLockUpdateRequest request = new UserLockUpdateRequest(true);

            willDoNothing().given(userFacade).updateLocked(
                eq(adminId),
                eq(targetUserId),
                any(UserLockUpdateRequest.class)
            );

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/locked", targetUserId)
                .with(user(mockAdminDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

            then(userFacade).should().updateLocked(
                eq(adminId),
                eq(targetUserId),
                any(UserLockUpdateRequest.class)
            );
        }

        @Test
        @DisplayName("locked가 null인 경우 400 Bad Request 응답")
        void withNullLocked_returns400BadRequest() throws Exception {
            // given
            UUID targetUserId = UUID.randomUUID();
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("locked", null);

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/locked", targetUserId)
                .with(user(mockAdminDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());

            then(userFacade).should(never()).updateLocked(any(), any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 수정 시 404 Not Found 응답")
        void withNonExistingUserId_returns404NotFound() throws Exception {
            // given
            UUID targetUserId = UUID.randomUUID();
            UserLockUpdateRequest request = new UserLockUpdateRequest(true);

            willThrow(UserNotFoundException.withId(targetUserId))
                .given(userFacade)
                .updateLocked(
                    eq(adminId),
                    eq(targetUserId),
                    any(UserLockUpdateRequest.class)
                );

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/locked", targetUserId)
                .with(user(mockAdminDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("자기 자신의 잠금 상태 변경 시 400 Bad Request 응답")
        void withSelfLockChange_returns400BadRequest() throws Exception {
            // given
            UserLockUpdateRequest request = new UserLockUpdateRequest(true);

            willThrow(SelfLockChangeException.withUserId(adminId))
                .given(userFacade)
                .updateLocked(
                    eq(adminId),
                    eq(adminId),
                    any(UserLockUpdateRequest.class)
                );

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/locked", adminId)
                .with(user(mockAdminDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{userId}/password - 비밀번호 변경")
    class UpdatePasswordTest {

        @Test
        @DisplayName("유효한 요청 시 204 No Content 응답")
        void withValidRequest_returns204NoContent() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            ChangePasswordRequest request = new ChangePasswordRequest("newP@ssw0rd!");

            willDoNothing().given(userFacade).updatePassword(eq(userId), eq("newP@ssw0rd!"));

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/password", userId)
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

            then(userFacade).should().updatePassword(eq(userId), eq("newP@ssw0rd!"));
        }

        @Test
        @DisplayName("비밀번호가 빈 문자열이면 400 Bad Request 응답")
        void withBlankPassword_returns400BadRequest() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("password", "");

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/password", userId)
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());

            then(userFacade).should(never()).updatePassword(any(), any());
        }

        @Test
        @DisplayName("비밀번호가 null이면 400 Bad Request 응답")
        void withNullPassword_returns400BadRequest() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("password", null);

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/password", userId)
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());

            then(userFacade).should(never()).updatePassword(any(), any());
        }

        @Test
        @DisplayName("비밀번호가 50자 초과이면 400 Bad Request 응답")
        void withPasswordExceeding50Chars_returns400BadRequest() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            ChangePasswordRequest request = new ChangePasswordRequest("a".repeat(51));

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/password", userId)
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            then(userFacade).should(never()).updatePassword(any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 요청 시 404 Not Found 응답")
        void withNonExistingUserId_returns404NotFound() throws Exception {
            // given
            UUID nonExistingUserId = UUID.randomUUID();
            ChangePasswordRequest request = new ChangePasswordRequest("newP@ssw0rd!");

            willThrow(UserNotFoundException.withId(nonExistingUserId))
                .given(userFacade)
                .updatePassword(eq(nonExistingUserId), eq("newP@ssw0rd!"));

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/password", nonExistingUserId)
                .with(user(mockUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }
}
