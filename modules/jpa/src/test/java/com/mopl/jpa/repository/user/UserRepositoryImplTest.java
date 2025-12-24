package com.mopl.jpa.repository.user;

import com.mopl.domain.model.user.AuthProvider;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.jpa.config.JpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaConfig.class, UserRepositoryImpl.class})
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
    }

    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmailTest {

        @Test
        @DisplayName("존재하는 이메일이면 true 반환")
        void withExistingEmail_returnsTrue() {
            // given
            String email = "existing@example.com";
            userRepository.save(UserModel.create(AuthProvider.EMAIL, email, "홍길동", "encodedPassword"));

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
