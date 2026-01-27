package com.mopl.jpa.entity.notification;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.repository.notification.JpaNotificationRepository;
import com.mopl.jpa.repository.notification.NotificationRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    NotificationRepositoryImpl.class,
    NotificationEntityMapper.class
})
@DisplayName("NotificationEntity 슬라이스 테스트")
class NotificationEntityTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private JpaNotificationRepository jpaNotificationRepository;

    private UUID receiverId;

    @BeforeEach
    void setUp() {
        UserEntity userEntity = UserEntity.builder()
            .authProvider(UserModel.AuthProvider.EMAIL)
            .email("test@example.com")
            .name("테스트")
            .password("encodedPassword")
            .role(UserModel.Role.USER)
            .locked(false)
            .build();
        testEntityManager.persistAndFlush(userEntity);
        receiverId = userEntity.getId();
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
    @DisplayName("receiverId 저장")
    class ReceiverIdTest {

        @Test
        @DisplayName("receiverId가 정상적으로 저장됨")
        void withReceiverId_savesCorrectly() {
            // given
            NotificationEntity entity = createNotificationEntity();
            testEntityManager.persistAndFlush(entity);
            testEntityManager.clear();

            // when
            NotificationEntity found = jpaNotificationRepository.findById(entity.getId())
                .orElseThrow();

            // then
            assertThat(found.getReceiverId()).isEqualTo(receiverId);
        }
    }

    private NotificationEntity createNotificationEntity() {
        return NotificationEntity.builder()
            .title("테스트 알림")
            .content("테스트 내용")
            .level(NotificationModel.NotificationLevel.INFO)
            .receiverId(receiverId)
            .build();
    }
}
