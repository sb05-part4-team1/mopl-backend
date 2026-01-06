package com.mopl.api.interfaces.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.api.application.user.UserFacade;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.domain.exception.user.DuplicateEmailException;
import com.mopl.domain.exception.user.InvalidUserDataException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.fixture.UserModelFixture;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiControllerAdvice.class, UserResponseMapper.class})
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
            UserModel userModel = UserModelFixture.create();

            given(userFacade.getUser(userModel.getId())).willReturn(userModel);

            // when & then
            mockMvc.perform(get("/api/users/{userId}", userModel.getId()))
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
            UUID userId = UUID.randomUUID();

            given(userFacade.getUser(userId)).willThrow(UserNotFoundException.withId(userId));

            // when & then
            mockMvc.perform(get("/api/users/{userId}", userId))
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
                .file(image))
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
                .file(requestPart))
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
                .file(image))
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
            UUID userId = UUID.randomUUID();

            MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
            );

            given(userFacade.updateProfile(
                eq(userId),
                isNull(),
                any(MultipartFile.class))
            ).willThrow(UserNotFoundException.withId(userId));

            // when & then
            mockMvc.perform(
                multipart(HttpMethod.PATCH, "/api/users/{userId}", userId).file(image)
            ).andExpect(status().isNotFound());
        }
    }
}
