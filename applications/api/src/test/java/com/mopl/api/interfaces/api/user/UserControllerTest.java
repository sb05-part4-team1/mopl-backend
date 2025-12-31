package com.mopl.api.interfaces.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.api.application.user.UserFacade;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.domain.exception.user.DuplicateEmailException;
import com.mopl.domain.exception.user.InvalidUserDataException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiControllerAdvice.class)
@DisplayName("UserController 슬라이스 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserFacade userFacade;

    @MockBean
    private UserResponseMapper userResponseMapper;

    @Nested
    @DisplayName("POST /api/users - 회원가입")
    class SignUpTest {

        @Test
        @DisplayName("유효한 요청 시 201 Created 응답")
        void withValidRequest_returns201Created() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();
            String email = "test@example.com";
            String name = "test";
            String password = "P@ssw0rd!";

            UserCreateRequest request = new UserCreateRequest(email, name, password);

            UserModel userModel = UserModel.builder()
                .id(userId)
                .createdAt(now)
                .deletedAt(null)
                .updatedAt(now)
                .authProvider(UserModel.AuthProvider.EMAIL)
                .email(email)
                .name(name)
                .password(password)
                .profileImageUrl(null)
                .role(UserModel.Role.USER)
                .locked(false)
                .build();

            UserResponse userResponse = new UserResponse(
                userId,
                now,
                email,
                name,
                null,
                UserModel.Role.USER,
                false
            );

            given(userFacade.signUp(any(UserCreateRequest.class))).willReturn(userModel);
            given(userResponseMapper.toResponse(userModel)).willReturn(userResponse);

            // when & then
            mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("test"))
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
            String requestBody = String.format(
                "{\"email\":%s,\"name\":%s,\"password\":%s}",
                email == null ? "null" : "\"" + email + "\"",
                name == null ? "null" : "\"" + name + "\"",
                password == null ? "null" : "\"" + password + "\""
            );

            // when & then
            mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
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
                .willThrow(new InvalidUserDataException("이메일은 비어있을 수 없습니다."));

            // when & then
            mockMvc.perform(post("/api/users")
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
            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();
            String email = "test@example.com";
            String name = "test";

            UserModel userModel = UserModel.builder()
                .id(userId)
                .createdAt(now)
                .deletedAt(null)
                .updatedAt(now)
                .authProvider(UserModel.AuthProvider.EMAIL)
                .email(email)
                .name(name)
                .password("encodedPassword")
                .profileImageUrl(null)
                .role(UserModel.Role.USER)
                .locked(false)
                .build();

            UserResponse userResponse = new UserResponse(
                userId,
                now,
                email,
                name,
                null,
                UserModel.Role.USER,
                false
            );

            given(userFacade.getUser(userId)).willReturn(userModel);
            given(userResponseMapper.toResponse(userModel)).willReturn(userResponse);

            // when & then
            mockMvc.perform(get("/api/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.locked").value(false));

            then(userFacade).should().getUser(userId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회 시 404 Not Found 응답")
        void withNonExistingUserId_returns404NotFound() throws Exception {
            // given
            UUID userId = UUID.randomUUID();

            given(userFacade.getUser(userId)).willThrow(UserNotFoundException.withId(userId));

            // when & then
            mockMvc.perform(get("/api/users/{userId}", userId))
                .andExpect(status().isNotFound());
        }
    }
}
