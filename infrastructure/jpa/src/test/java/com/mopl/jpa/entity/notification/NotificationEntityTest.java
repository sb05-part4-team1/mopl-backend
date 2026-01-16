package com.mopl.jpa.entity.notification;

import com.mopl.domain.model.notification.NotificationLevel;
import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.entity.user.UserEntityMapper;
import com.mopl.jpa.repository.notification.JpaNotificationRepository;
import com.mopl.jpa.repository.notification.NotificationRepositoryImpl;
import com.mopl.jpa.repository.user.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    JpaConfig.class,
    NotificationRepositoryImpl.class,
    NotificationEntityMapper.class,
    UserEntityMapper.class
})
@DisplayName("NotificationEntity 슬라이스 테스트")
class NotificationEntityTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaNotificationRepository jpaNotificationRepository;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
            .authProvider(UserModel.AuthProvider.EMAIL)
            .email("test@example.com")
            .name("테스트")
            .password("encodedPassword")
            .role(UserModel.Role.USER)
            .locked(false)
            .build();
        testEntityManager.persistAndFlush(userEntity);
    }

    @Nested
    @DisplayName("엔티티 저장")
    class PersistTest {

        @Test
        @DisplayName("저장 시 id가 UUID v7으로 자동 생성됨")
        void withPersist_generatesUuidV7() {
            // given
            NotificationEntity entity = createNotificationEntity();

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
            NotificationEntity entity = createNotificationEntity();

            // when
            testEntityManager.persistAndFlush(entity);

            // then
            assertThat(entity.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("@SQLRestriction")
    class SoftDeleteTest {

        @Test
        @DisplayName("deletedAt이 null이 아닌 엔티티는 조회되지 않음")
        void withDeletedAt_excludesFromQuery() {
            // given
            NotificationEntity entity = NotificationEntity.builder()
                .title("삭제된 알림")
                .content("삭제된 내용")
                .level(NotificationLevel.INFO)
                .receiver(userEntity)
                .deletedAt(Instant.now())
                .build();
            testEntityManager.persistAndFlush(entity);
            testEntityManager.clear();

            // when
            Optional<NotificationModel> result = notificationRepository.findById(entity.getId());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("deletedAt이 null인 엔티티는 정상 조회됨")
        void withoutDeletedAt_includesInQuery() {
            // given
            NotificationEntity entity = createNotificationEntity();
            testEntityManager.persistAndFlush(entity);
            testEntityManager.clear();

            // when
            Optional<NotificationModel> result = notificationRepository.findById(entity.getId());

            // then
            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("연관 관계")
    class RelationshipTest {

        @Test
        @DisplayName("receiver와 ManyToOne 관계가 정상 동작함")
        void withReceiver_manyToOneRelationship() {
            // given
            NotificationEntity entity = createNotificationEntity();
            testEntityManager.persistAndFlush(entity);
            testEntityManager.clear();

            // when
            NotificationEntity found = jpaNotificationRepository.findById(entity.getId())
                .orElseThrow();

            // then
            assertThat(found.getReceiver()).isNotNull();
            assertThat(found.getReceiver().getId()).isEqualTo(userEntity.getId());
        }
    }

    private NotificationEntity createNotificationEntity() {
        return NotificationEntity.builder()
            .title("테스트 알림")
            .content("테스트 내용")
            .level(NotificationLevel.INFO)
            .receiver(userEntity)
            .build();
    }
}
