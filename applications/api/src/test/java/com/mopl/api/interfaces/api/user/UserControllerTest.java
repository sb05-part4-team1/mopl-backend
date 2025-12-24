package com.mopl.api.interfaces.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.api.application.user.UserFacade;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.domain.exception.user.DuplicateEmailException;
import com.mopl.domain.exception.user.InvalidUserDataException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.model.user.Role;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({ApiControllerAdvice.class, UserControllerAdvice.class})
@DisplayName("UserController 슬라이스 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserFacade userFacade;

    @Nested
    @DisplayName("POST /api/users - 회원가입")
    class SignUpTest {

        @Test
        @DisplayName("유효한 요청 시 201 Created 응답")
        void withValidRequest_returns201Created() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest(
                "test",
                "test@example.com",
                "P@ssw0rd!"
            );

            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();
            UserDto userDto = new UserDto(
                userId,
                now,
                "test@example.com",
                "test",
                null,
                Role.USER,
                false
            );

            given(userFacade.signUp(any(UserCreateRequest.class))).willReturn(userDto);

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
                Arguments.of("이름이 비어있음", "", "test@example.com", "P@ssw0rd!"),
                Arguments.of("이름이 null", null, "test@example.com", "P@ssw0rd!"),
                Arguments.of("이름이 50자 초과", "a".repeat(51), "test@example.com", "P@ssw0rd!"),
                Arguments.of("이메일이 비어있음", "test", "", "P@ssw0rd!"),
                Arguments.of("이메일이 null", "test", null, "P@ssw0rd!"),
                Arguments.of("이메일이 255자 초과", "test", "a".repeat(256), "P@ssw0rd!"),
                Arguments.of("비밀번호가 비어있음", "test", "test@example.com", ""),
                Arguments.of("비밀번호가 null", "test", "test@example.com", null),
                Arguments.of("비밀번호가 50자 초과", "test", "test@example.com", "a".repeat(51))
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidRequestProvider")
        @DisplayName("유효하지 않은 요청 시 400 Bad Request 응답")
        void withInvalidRequest_returns400BadRequest(
            String description,
            String name,
            String email,
            String password
        ) throws Exception {
            // given
            String requestBody = String.format(
                "{\"name\":%s,\"email\":%s,\"password\":%s}",
                name == null ? "null" : "\"" + name + "\"",
                email == null ? "null" : "\"" + email + "\"",
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
        @DisplayName("요청 본문이 없으면 400 Bad Request 응답")
        void withoutRequestBody_returns400BadRequest() throws Exception {
            // when & then
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

            then(userFacade).should(never()).signUp(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("유효하지 않은 사용자 데이터면 400 Bad Request 응답")
        void withInvalidUserData_returns400BadRequest() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest(
                "test",
                "test@example.com",
                "P@ssw0rd!"
            );

            given(userFacade.signUp(any(UserCreateRequest.class)))
                .willThrow(new InvalidUserDataException("이메일은 255자를 초과할 수 없습니다."));

            // when & then
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            then(userFacade).should().signUp(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("이미 가입된 이메일이면 409 Conflict 응답")
        void withDuplicateEmail_returns409Conflict() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest(
                "test",
                "test@example.com",
                "P@ssw0rd!"
            );

            given(userFacade.signUp(any(UserCreateRequest.class)))
                .willThrow(new DuplicateEmailException("test@example.com"));

            // when & then
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

            then(userFacade).should().signUp(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("사용자를 찾을 수 없으면 404 Not Found 응답")
        void withUserNotFound_returns404NotFound() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UserCreateRequest request = new UserCreateRequest(
                "test",
                "test@example.com",
                "P@ssw0rd!"
            );

            given(userFacade.signUp(any(UserCreateRequest.class)))
                .willThrow(new UserNotFoundException(userId));

            // when & then
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

            then(userFacade).should().signUp(any(UserCreateRequest.class));
        }
    }
}
