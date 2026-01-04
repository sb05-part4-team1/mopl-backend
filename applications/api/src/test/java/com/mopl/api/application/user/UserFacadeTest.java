package com.mopl.api.application.user;

import com.mopl.api.interfaces.api.user.UserCreateRequest;
import com.mopl.api.interfaces.api.user.UserRoleUpdateRequest;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.user.UserService;
import com.mopl.storage.provider.FileStorageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserFacade 단위 테스트")
class UserFacadeTest {

    @Mock
    private UserService userService;

    @Mock
    private FileStorageProvider fileStorageProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserFacade userFacade;

    @Nested
    @DisplayName("signUp()")
    class SignUpTest {

        @Test
        @DisplayName("유효한 요청 시 회원가입 성공")
        void withValidRequest_signUpSuccess() {
            // given
            String email = "test@example.com";
            String name = "test";
            String password = "P@ssw0rd!";
            String encodedPassword = "encodedPassword";
            UserCreateRequest request = new UserCreateRequest(email, name, password);

            UserModel savedUserModel = UserModelFixture.builder()
                .set("email", email)
                .set("name", name)
                .set("password", encodedPassword)
                .sample();

            given(passwordEncoder.encode(password)).willReturn(encodedPassword);
            given(userService.create(any(UserModel.class))).willReturn(savedUserModel);

            // when
            UserModel result = userFacade.signUp(request);

            // then
            assertThat(result.getId()).isEqualTo(savedUserModel.getId());
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getName()).isEqualTo(name);
            assertThat(result.getRole()).isEqualTo(UserModel.Role.USER);
            assertThat(result.isLocked()).isFalse();

            then(userService).should().create(any(UserModel.class));
        }

        @Test
        @DisplayName("이메일과 이름의 공백이 제거되고 이메일이 소문자로 처리된다")
        void withWhitespace_shouldTrimAndLowercase() {
            // given
            String email = "  TEST@EXAMPLE.COM  ";
            String name = "  test  ";
            String password = "P@ssw0rd!";
            String encodedPassword = "encodedPassword";
            UserCreateRequest request = new UserCreateRequest(email, name, password);

            String expectedEmail = "test@example.com";
            String expectedName = "test";

            UserModel savedUserModel = UserModelFixture.builder()
                .set("email", expectedEmail)
                .set("name", expectedName)
                .set("password", encodedPassword)
                .sample();

            given(passwordEncoder.encode(password)).willReturn(encodedPassword);
            given(userService.create(any(UserModel.class))).willReturn(savedUserModel);

            // when
            UserModel result = userFacade.signUp(request);

            // then
            assertThat(result.getEmail()).isEqualTo(expectedEmail);
            assertThat(result.getName()).isEqualTo(expectedName);
        }
    }

    @Nested
    @DisplayName("getUser()")
    class GetUserTest {

        @Test
        @DisplayName("유효한 요청 시 사용자 상세 조회 성공")
        void withValidRequest_getUserSuccess() {
            // given
            UserModel userModel = UserModelFixture.create();

            given(userService.getById(userModel.getId())).willReturn(userModel);

            // when
            UserModel result = userFacade.getUser(userModel.getId());

            // then
            assertThat(result.getId()).isEqualTo(userModel.getId());
            assertThat(result.getEmail()).isEqualTo(userModel.getEmail());
            assertThat(result.getName()).isEqualTo(userModel.getName());
            assertThat(result.getRole()).isEqualTo(UserModel.Role.USER);
            assertThat(result.isLocked()).isFalse();

            then(userService).should().getById(userModel.getId());
        }
    }

    @Nested
    @DisplayName("updateRoleInternal()")
    class UpdateRoleInternalTest {

        @Test
        @DisplayName("유효한 요청 시 역할 업데이트 성공")
        void withValidRequest_updateRoleSuccess() {
            // given
            UserModel userModel = UserModelFixture.create();

            UserModel updatedUserModel = UserModelFixture.builder()
                .set("id", userModel.getId())
                .set("role", UserModel.Role.ADMIN)
                .sample();

            UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserModel.Role.ADMIN);

            given(userService.getById(userModel.getId())).willReturn(userModel);
            given(userService.update(any(UserModel.class))).willReturn(updatedUserModel);

            // when
            UserModel result = userFacade.updateRoleInternal(request, userModel.getId());

            // then
            assertThat(result.getId()).isEqualTo(userModel.getId());
            assertThat(result.getRole()).isEqualTo(UserModel.Role.ADMIN);

            then(userService).should().getById(userModel.getId());
            then(userService).should().update(any(UserModel.class));
        }
    }

    @Nested
    @DisplayName("updateProfile()")
    class UpdateProfileTest {

        @Test
        @DisplayName("유효한 프로필 이미지로 수정 시 프로필 업데이트 성공")
        void withValidProfileImage_updateProfileSuccess() throws IOException {
            // given
            UserModel userModel = UserModelFixture.create();
            String storedPath = "users/" + userModel.getId() + "/test.png";
            String profileImageUrl = "http://localhost/api/v1/files/display?path=" + storedPath;

            MultipartFile image = mock(MultipartFile.class);
            given(image.isEmpty()).willReturn(false);
            given(image.getInputStream()).willReturn(new ByteArrayInputStream("test".getBytes()));
            given(image.getOriginalFilename()).willReturn("test.png");

            UserModel updatedUserModel = UserModelFixture.builder()
                .set("id", userModel.getId())
                .set("profileImageUrl", profileImageUrl)
                .sample();

            given(userService.getById(userModel.getId())).willReturn(userModel);
            given(fileStorageProvider.upload(any(), anyString())).willReturn(storedPath);
            given(fileStorageProvider.getUrl(storedPath)).willReturn(profileImageUrl);
            given(userService.update(any(UserModel.class))).willReturn(updatedUserModel);

            // when
            UserModel result = userFacade.updateProfile(userModel.getId(), image);

            // then
            assertThat(result.getProfileImageUrl()).isEqualTo(profileImageUrl);

            then(userService).should().getById(userModel.getId());
            then(fileStorageProvider).should().upload(any(), anyString());
            then(fileStorageProvider).should().getUrl(storedPath);
            then(userService).should().update(any(UserModel.class));
        }

        @Test
        @DisplayName("이미지가 null이면 파일 업로드 없이 업데이트")
        void withNullImage_updateWithoutFileUpload() {
            // given
            UserModel userModel = UserModelFixture.create();

            given(userService.getById(userModel.getId())).willReturn(userModel);
            given(userService.update(any(UserModel.class))).willReturn(userModel);

            // when
            UserModel result = userFacade.updateProfile(userModel.getId(), null);

            // then
            assertThat(result.getId()).isEqualTo(userModel.getId());

            then(userService).should().getById(userModel.getId());
            then(fileStorageProvider).should(never()).upload(any(), anyString());
            then(userService).should().update(any(UserModel.class));
        }

        @Test
        @DisplayName("이미지가 비어있으면 파일 업로드 없이 업데이트")
        void withEmptyImage_updateWithoutFileUpload() {
            // given
            UserModel userModel = UserModelFixture.create();

            MultipartFile image = mock(MultipartFile.class);
            given(image.isEmpty()).willReturn(true);

            given(userService.getById(userModel.getId())).willReturn(userModel);
            given(userService.update(any(UserModel.class))).willReturn(userModel);

            // when
            UserModel result = userFacade.updateProfile(userModel.getId(), image);

            // then
            assertThat(result.getId()).isEqualTo(userModel.getId());

            then(userService).should().getById(userModel.getId());
            then(fileStorageProvider).should(never()).upload(any(), anyString());
            then(userService).should().update(any(UserModel.class));
        }

        @Test
        @DisplayName("파일 스트림 읽기 실패 시 UncheckedIOException 발생")
        void withIOException_throwsUncheckedIOException() throws IOException {
            // given
            UserModel userModel = UserModelFixture.create();

            MultipartFile image = mock(MultipartFile.class);
            given(image.isEmpty()).willReturn(false);
            given(image.getInputStream()).willThrow(new IOException("파일 읽기 실패"));

            given(userService.getById(userModel.getId())).willReturn(userModel);

            // when & then
            assertThatThrownBy(() -> userFacade.updateProfile(userModel.getId(), image))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("파일 스트림 읽기 실패");

            then(fileStorageProvider).should(never()).upload(any(), anyString());
            then(userService).should(never()).update(any(UserModel.class));
        }
    }
}
