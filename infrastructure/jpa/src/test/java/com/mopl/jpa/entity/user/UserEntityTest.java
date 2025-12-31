package com.mopl.jpa.entity.user;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.repository.user.UserRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    JpaConfig.class,
    UserRepositoryImpl.class,
    UserEntityMapper.class
})
@DisplayName("UserEntity 슬라이스 테스트")
class UserEntityTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("엔티티 저장")
    class PersistTest {

        @Test
        @DisplayName("저장 시 id가 UUID v7으로 자동 생성됨")
        void withPersist_generatesUuidV7() {
            // given
            UserEntity entity = createUserEntity();

            // when
            testEntityManager.persistAndFlush(entity);

            // then
            assertThat(entity.getId()).isNotNull();
            assertThat(entity.getId().version()).isEqualTo(7);
        }

        @Test
        @DisplayName("저장 시 createdAt이 자동 설정됨")
        void withPersist_setsCreatedAt() {
            // given
            UserEntity entity = createUserEntity();

            // when
            testEntityManager.persistAndFlush(entity);

            // then
            assertThat(entity.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("저장 시 updatedAt이 자동 설정됨")
        void withPersist_setsUpdatedAt() {
            // given
            UserEntity entity = createUserEntity();

            // when
            testEntityManager.persistAndFlush(entity);

            // then
            assertThat(entity.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("엔티티 수정")
    class UpdateTest {

        @Test
        @DisplayName("수정 시 dirty checking 후 updatedAt이 갱신됨")
        void withUpdate_updatesUpdatedAt() {
            // given
            UserEntity entity = createUserEntity();
            testEntityManager.persistAndFlush(entity);
            Instant originalUpdatedAt = entity.getUpdatedAt();

            // when
            ReflectionTestUtils.setField(entity, "name", "수정된이름");

            testEntityManager.flush();
            testEntityManager.clear();

            // then
            UserEntity updated = testEntityManager.find(UserEntity.class, entity.getId());
            assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
            assertThat(updated.getName()).isEqualTo("수정된이름");
        }
    }

    @Nested
    @DisplayName("@SQLRestriction")
    class SoftDeleteTest {

        @Test
        @DisplayName("deletedAt이 null이 아닌 엔티티는 조회되지 않음")
        void withDeletedAt_excludesFromQuery() {
            // given
            UserEntity entity = UserEntity.builder()
                .authProvider(UserModel.AuthProvider.EMAIL)
                .email("deleted@example.com")
                .name("삭제된사용자")
                .password("encodedPassword")
                .role(UserModel.Role.USER)
                .locked(false)
                .deletedAt(Instant.now())
                .build();
            testEntityManager.persistAndFlush(entity);
            testEntityManager.clear();

            // when
            Optional<UserModel> result = userRepository.findById(entity.getId());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("deletedAt이 null인 엔티티는 정상 조회됨")
        void withoutDeletedAt_includesInQuery() {
            // given
            UserEntity entity = createUserEntity();
            testEntityManager.persistAndFlush(entity);
            testEntityManager.clear();

            // when
            Optional<UserModel> result = userRepository.findById(entity.getId());

            // then
            assertThat(result).isPresent();
        }
    }

    private UserEntity createUserEntity() {
        return UserEntity.builder()
            .authProvider(UserModel.AuthProvider.EMAIL)
            .email("test@example.com")
            .name("테스트")
            .password("encodedPassword")
            .role(UserModel.Role.USER)
            .locked(false)
            .build();
    }
}
