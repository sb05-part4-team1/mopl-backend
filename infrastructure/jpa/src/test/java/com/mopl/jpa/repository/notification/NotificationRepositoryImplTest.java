package com.mopl.jpa.repository.notification;

import com.mopl.domain.model.notification.NotificationModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.notification.NotificationRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.notification.NotificationEntityMapper;
import com.mopl.jpa.entity.user.UserEntityMapper;
import com.mopl.jpa.repository.user.UserRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    NotificationRepositoryImpl.class,
    NotificationEntityMapper.class,
    UserRepositoryImpl.class,
    UserEntityMapper.class
})
@DisplayName("NotificationRepositoryImpl 슬라이스 테스트")
class NotificationRepositoryImplTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID savedReceiverId;

    @BeforeEach
    void setUp() {
        UserModel savedReceiver = userRepository.save(
            UserModel.create(
                UserModel.AuthProvider.EMAIL,
                "receiver@example.com",
                "수신자",
                "encodedPassword"
            )
        );
        savedReceiverId = savedReceiver.getId();
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 알림 ID로 조회하면 NotificationModel을 반환한다")
        void withExistingId_returnsNotificationModel() {
            // given
            NotificationModel savedNotification = notificationRepository.save(
                NotificationModel.create(
                    "새로운 알림",
                    "알림 내용입니다.",
                    NotificationModel.NotificationLevel.INFO,
                    savedReceiverId
                )
            );

            // when
            Optional<NotificationModel> foundNotification = notificationRepository.findById(
                savedNotification.getId());

            // then
            assertThat(foundNotification).isPresent();
            assertThat(foundNotification.get().getId()).isEqualTo(savedNotification.getId());
            assertThat(foundNotification.get().getTitle()).isEqualTo("새로운 알림");
            assertThat(foundNotification.get().getContent()).isEqualTo("알림 내용입니다.");
            assertThat(foundNotification.get().getLevel()).isEqualTo(
                NotificationModel.NotificationLevel.INFO);
        }

        @Test
        @DisplayName("존재하지 않는 알림 ID로 조회하면 빈 Optional을 반환한다")
        void withNonExistingId_returnsEmptyOptional() {
            // given
            UUID nonExistingId = UUID.randomUUID();

            // when
            Optional<NotificationModel> foundNotification = notificationRepository.findById(
                nonExistingId);

            // then
            assertThat(foundNotification).isEmpty();
        }

        @Test
        @DisplayName("조회 결과에 수신자 ID가 포함된다")
        void withExistingId_includesReceiverId() {
            // given
            NotificationModel savedNotification = notificationRepository.save(
                NotificationModel.create(
                    "수신자 테스트",
                    "수신자 확인용 알림",
                    NotificationModel.NotificationLevel.WARNING,
                    savedReceiverId
                )
            );

            // when
            Optional<NotificationModel> foundNotification = notificationRepository.findById(
                savedNotification.getId());

            // then
            assertThat(foundNotification).isPresent();
            assertThat(foundNotification.get().getReceiverId()).isEqualTo(savedReceiverId);
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("새 알림을 저장하고 반환한다")
        void withNewNotification_savesAndReturnsNotification() {
            // given
            NotificationModel notificationModel = NotificationModel.create(
                "테스트 알림",
                "테스트 알림 내용입니다.",
                NotificationModel.NotificationLevel.INFO,
                savedReceiverId
            );

            // when
            NotificationModel savedNotification = notificationRepository.save(notificationModel);

            // then
            assertThat(savedNotification.getId()).isNotNull();
            assertThat(savedNotification.getTitle()).isEqualTo("테스트 알림");
            assertThat(savedNotification.getContent()).isEqualTo("테스트 알림 내용입니다.");
            assertThat(savedNotification.getLevel()).isEqualTo(
                NotificationModel.NotificationLevel.INFO);
            assertThat(savedNotification.getReceiverId()).isEqualTo(savedReceiverId);
            assertThat(savedNotification.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("WARNING 레벨 알림을 저장할 수 있다")
        void withWarningLevel_savesSuccessfully() {
            // given
            NotificationModel notificationModel = NotificationModel.create(
                "경고 알림",
                "경고 내용입니다.",
                NotificationModel.NotificationLevel.WARNING,
                savedReceiverId
            );

            // when
            NotificationModel savedNotification = notificationRepository.save(notificationModel);

            // then
            assertThat(savedNotification.getId()).isNotNull();
            assertThat(savedNotification.getLevel()).isEqualTo(
                NotificationModel.NotificationLevel.WARNING);
        }

        @Test
        @DisplayName("ERROR 레벨 알림을 저장할 수 있다")
        void withErrorLevel_savesSuccessfully() {
            // given
            NotificationModel notificationModel = NotificationModel.create(
                "에러 알림",
                "에러 내용입니다.",
                NotificationModel.NotificationLevel.ERROR,
                savedReceiverId
            );

            // when
            NotificationModel savedNotification = notificationRepository.save(notificationModel);

            // then
            assertThat(savedNotification.getId()).isNotNull();
            assertThat(savedNotification.getLevel()).isEqualTo(
                NotificationModel.NotificationLevel.ERROR);
        }

        @Test
        @DisplayName("내용이 null인 알림도 저장할 수 있다")
        void withNullContent_savesSuccessfully() {
            // given
            NotificationModel notificationModel = NotificationModel.create(
                "내용 없는 알림",
                null,
                NotificationModel.NotificationLevel.INFO,
                savedReceiverId
            );

            // when
            NotificationModel savedNotification = notificationRepository.save(notificationModel);

            // then
            assertThat(savedNotification.getId()).isNotNull();
            assertThat(savedNotification.getTitle()).isEqualTo("내용 없는 알림");
            assertThat(savedNotification.getContent()).isNull();
        }

        @Test
        @DisplayName("삭제된 알림을 저장하면 deletedAt이 유지된다")
        void withDeletedNotification_preservesDeletedAt() {
            // given
            NotificationModel notificationModel = NotificationModel.create(
                "삭제될 알림",
                "삭제 테스트용",
                NotificationModel.NotificationLevel.INFO,
                savedReceiverId
            );
            NotificationModel savedNotification = notificationRepository.save(notificationModel);
            savedNotification.delete();

            // when
            NotificationModel updatedNotification = notificationRepository.save(savedNotification);

            // then
            assertThat(updatedNotification.isDeleted()).isTrue();
            assertThat(updatedNotification.getDeletedAt()).isNotNull();
        }
    }
}
