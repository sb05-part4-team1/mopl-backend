package com.mopl.domain.service.user;

import com.mopl.domain.exception.user.DuplicateEmailException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.model.user.UserModel.AuthProvider;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 사용자 생성")
        void withValidUser_createsUser() {
            // given
            UserModel userModel = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "encodedPassword"
            );

            given(userRepository.existsByEmail("test@example.com")).willReturn(false);
            given(userRepository.save(userModel)).willReturn(userModel);

            // when
            UserModel result = userService.create(userModel);

            // then
            assertThat(result).isEqualTo(userModel);
            then(userRepository).should().existsByEmail("test@example.com");
            then(userRepository).should().save(userModel);
        }

        @Test
        @DisplayName("중복 이메일이면 예외 발생")
        void withDuplicateEmail_throwsException() {
            // given
            UserModel userModel = UserModel.create(
                AuthProvider.EMAIL,
                "duplicate@example.com",
                "홍길동",
                "encodedPassword"
            );

            given(userRepository.existsByEmail("duplicate@example.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.create(userModel))
                .isInstanceOf(DuplicateEmailException.class)
                .satisfies(e -> {
                    DuplicateEmailException ex = (DuplicateEmailException) e;
                    assertThat(ex.getDetails().get("email")).isEqualTo("duplicate@example.com");
                });

            then(userRepository).should().existsByEmail("duplicate@example.com");
            then(userRepository).should(never()).save(any(UserModel.class));
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTest {

        @Test
        @DisplayName("존재하는 사용자 ID로 조회하면 UserModel 반환")
        void withExistingUserId_returnsUserModel() {
            // given
            UUID userId = UUID.randomUUID();
            UserModel userModel = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "encodedPassword"
            );

            given(userRepository.findById(userId)).willReturn(Optional.of(userModel));

            // when
            UserModel result = userService.getById(userId);

            // then
            assertThat(result).isEqualTo(userModel);
            then(userRepository).should().findById(userId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회하면 UserNotFoundException 발생")
        void withNonExistingUserId_throwsUserNotFoundException() {
            // given
            UUID userId = UUID.randomUUID();

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .satisfies(e -> {
                    UserNotFoundException ex = (UserNotFoundException) e;
                    assertThat(ex.getDetails().get("id")).isEqualTo(userId);
                });

            then(userRepository).should().findById(userId);
        }
    }

    @Nested
    @DisplayName("getByEmail()")
    class GetByEmailTest {

        @Test
        @DisplayName("존재하는 이메일로 조회하면 UserModel 반환")
        void withExistingEmail_returnsUserModel() {
            // given
            String email = "test@example.com";
            UserModel userModel = UserModel.create(
                AuthProvider.EMAIL,
                email,
                "홍길동",
                "encodedPassword"
            );

            given(userRepository.findByEmail(email)).willReturn(Optional.of(userModel));

            // when
            UserModel result = userService.getByEmail(email);

            // then
            assertThat(result).isEqualTo(userModel);
            then(userRepository).should().findByEmail(email);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회하면 UserNotFoundException 발생")
        void withNonExistingEmail_throwsUserNotFoundException() {
            // given
            String email = "nonexistent@example.com";

            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getByEmail(email))
                .isInstanceOf(UserNotFoundException.class)
                .satisfies(e -> {
                    UserNotFoundException ex = (UserNotFoundException) e;
                    assertThat(ex.getDetails().get("email")).isEqualTo(email);
                });

            then(userRepository).should().findByEmail(email);
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("사용자 정보 업데이트 성공")
        void withValidUser_updatesUser() {
            // given
            UserModel userModel = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "encodedPassword"
            );

            given(userRepository.save(userModel)).willReturn(userModel);

            // when
            UserModel result = userService.update(userModel);

            // then
            assertThat(result).isEqualTo(userModel);
            then(userRepository).should().save(userModel);
        }
    }
}
