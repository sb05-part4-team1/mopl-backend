package com.mopl.jpa.repository.user;

import com.mopl.domain.model.user.UserModel.AuthProvider;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.user.UserEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    JpaConfig.class,
    UserRepositoryImpl.class,
    UserEntityMapper.class
})
@DisplayName("UserRepositoryImpl 슬라이스 테스트")
class UserRepositoryImplTest {

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("새 사용자 저장")
        void withNewUser_savesAndReturnsUser() {
            // given
            UserModel userModel = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "encodedPassword"
            );

            // when
            UserModel savedUser = userRepository.save(userModel);

            // then
            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
            assertThat(savedUser.getName()).isEqualTo("홍길동");
            assertThat(savedUser.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("기존 사용자 업데이트")
        void withExistingUser_updatesAndReturnsUser() {
            // given
            UserModel userModel = UserModel.create(
                AuthProvider.EMAIL,
                "test@example.com",
                "홍길동",
                "encodedPassword"
            );
            UserModel savedUser = userRepository.save(userModel);

            // when
            savedUser.updateRole(UserModel.Role.ADMIN);
            UserModel updatedUser = userRepository.save(savedUser);

            // then
            assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
            assertThat(updatedUser.getRole()).isEqualTo(UserModel.Role.ADMIN);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 사용자 ID로 조회하면 UserModel 반환")
        void withExistingUserId_returnsUserModel() {
            // given
            UserModel savedUser = userRepository.save(
                UserModel.create(
                    AuthProvider.EMAIL,
                    "test@example.com",
                    "홍길동",
                    "encodedPassword"
                )
            );

            // when
            Optional<UserModel> foundUser = userRepository.findById(savedUser.getId());

            // then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
            assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
            assertThat(foundUser.get().getName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회하면 빈 Optional 반환")
        void withNonExistingUserId_returnsEmptyOptional() {
            // given
            UUID nonExistingId = UUID.randomUUID();

            // when
            Optional<UserModel> foundUser = userRepository.findById(nonExistingId);

            // then
            assertThat(foundUser).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmailTest {

        @Test
        @DisplayName("존재하는 이메일로 조회하면 UserModel 반환")
        void withExistingEmail_returnsUserModel() {
            // given
            String email = "test@example.com";
            UserModel savedUser = userRepository.save(
                UserModel.create(
                    AuthProvider.EMAIL,
                    email,
                    "홍길동",
                    "encodedPassword"
                )
            );

            // when
            Optional<UserModel> foundUser = userRepository.findByEmail(email);

            // then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
            assertThat(foundUser.get().getEmail()).isEqualTo(email);
            assertThat(foundUser.get().getName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional 반환")
        void withNonExistingEmail_returnsEmptyOptional() {
            // when
            Optional<UserModel> foundUser = userRepository.findByEmail("nonexisting@example.com");

            // then
            assertThat(foundUser).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmailTest {

        @Test
        @DisplayName("존재하는 이메일이면 true 반환")
        void withExistingEmail_returnsTrue() {
            // given
            String email = "existing@example.com";
            userRepository.save(
                UserModel.create(
                    AuthProvider.EMAIL,
                    email,
                    "홍길동",
                    "encodedPassword"
                )
            );

            // when
            boolean exists = userRepository.existsByEmail(email);

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 이메일이면 false 반환")
        void withNonExistingEmail_returnsFalse() {
            // when
            boolean exists = userRepository.existsByEmail("nonexisting@example.com");

            // then
            assertThat(exists).isFalse();
        }
    }
}
