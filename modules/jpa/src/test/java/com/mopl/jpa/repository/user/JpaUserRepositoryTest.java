package com.mopl.jpa.repository.user;

import com.mopl.domain.model.user.AuthProvider;
import com.mopl.domain.model.user.Role;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.user.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("JpaUserRepository 슬라이스 테스트")
class JpaUserRepositoryTest {

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("새 사용자 저장")
        void withNewUser_savesUser() {
            // given
            UserEntity entity = UserEntity.builder()
                .authProvider(AuthProvider.EMAIL)
                .email("test@example.com")
                .name("홍길동")
                .password("encodedPassword")
                .role(Role.USER)
                .locked(false)
                .build();

            // when
            UserEntity savedEntity = jpaUserRepository.save(entity);

            // then
            assertThat(savedEntity.getId()).isNotNull();
            assertThat(savedEntity.getCreatedAt()).isNotNull();
            assertThat(savedEntity.getEmail()).isEqualTo("test@example.com");
            assertThat(savedEntity.getName()).isEqualTo("홍길동");
            assertThat(savedEntity.getAuthProvider()).isEqualTo(AuthProvider.EMAIL);
            assertThat(savedEntity.getRole()).isEqualTo(Role.USER);
            assertThat(savedEntity.isLocked()).isFalse();
        }

        @Test
        @DisplayName("프로필 이미지 URL이 있는 사용자 저장")
        void withProfileImageUrl_savesUser() {
            // given
            UserEntity entity = UserEntity.builder()
                .authProvider(AuthProvider.GOOGLE)
                .email("google@example.com")
                .name("김철수")
                .password("encodedPassword")
                .profileImageUrl("https://example.com/profile.jpg")
                .role(Role.USER)
                .locked(false)
                .build();

            // when
            UserEntity savedEntity = jpaUserRepository.save(entity);

            // then
            assertThat(savedEntity.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
        }
    }

    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmailTest {

        @Test
        @DisplayName("존재하는 이메일이면 true 반환")
        void withExistingEmail_returnsTrue() {
            // given
            UserEntity entity = UserEntity.builder()
                .authProvider(AuthProvider.EMAIL)
                .email("existing@example.com")
                .name("홍길동")
                .password("encodedPassword")
                .role(Role.USER)
                .locked(false)
                .build();
            jpaUserRepository.save(entity);

            // when
            boolean exists = jpaUserRepository.existsByEmail("existing@example.com");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 이메일이면 false 반환")
        void withNonExistingEmail_returnsFalse() {
            // when
            boolean exists = jpaUserRepository.existsByEmail("nonexisting@example.com");

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 ID로 조회")
        void withExistingId_returnsUser() {
            // given
            UserEntity entity = UserEntity.builder()
                .authProvider(AuthProvider.EMAIL)
                .email("test@example.com")
                .name("홍길동")
                .password("encodedPassword")
                .role(Role.USER)
                .locked(false)
                .build();
            UserEntity savedEntity = jpaUserRepository.save(entity);

            // when
            var foundEntity = jpaUserRepository.findById(savedEntity.getId());

            // then
            assertThat(foundEntity).isPresent();
            assertThat(foundEntity.get().getEmail()).isEqualTo("test@example.com");
        }
    }
}
