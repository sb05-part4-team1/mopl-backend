package com.mopl.domain.model.watchingsession;

import com.mopl.domain.exception.watchingsession.InvalidWatchingSessionDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WatchingSessionModel 단위 테스트")
class WatchingSessionModelTest {

    private static final UUID DEFAULT_WATCHER_ID = UUID.randomUUID();
    private static final String DEFAULT_WATCHER_NAME = "시청자";
    private static final String DEFAULT_WATCHER_PROFILE_IMAGE_PATH = "users/123/profile.jpg";
    private static final UUID DEFAULT_CONTENT_ID = UUID.randomUUID();
    private static final String DEFAULT_CONTENT_TITLE = "테스트 콘텐츠";

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 WatchingSessionModel 생성")
        void withValidData_createsWatchingSessionModel() {
            // when
            WatchingSessionModel session = WatchingSessionModel.create(
                DEFAULT_WATCHER_ID,
                DEFAULT_WATCHER_NAME,
                DEFAULT_WATCHER_PROFILE_IMAGE_PATH,
                DEFAULT_CONTENT_ID,
                DEFAULT_CONTENT_TITLE
            );

            // then
            assertThat(session.getWatcherId()).isEqualTo(DEFAULT_WATCHER_ID);
            assertThat(session.getWatcherName()).isEqualTo(DEFAULT_WATCHER_NAME);
            assertThat(session.getWatcherProfileImagePath()).isEqualTo(DEFAULT_WATCHER_PROFILE_IMAGE_PATH);
            assertThat(session.getContentId()).isEqualTo(DEFAULT_CONTENT_ID);
            assertThat(session.getContentTitle()).isEqualTo(DEFAULT_CONTENT_TITLE);
            assertThat(session.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("watcherName이 null이어도 생성 성공")
        void withNullWatcherName_createsWatchingSessionModel() {
            // when
            WatchingSessionModel session = WatchingSessionModel.create(
                DEFAULT_WATCHER_ID,
                null,
                DEFAULT_WATCHER_PROFILE_IMAGE_PATH,
                DEFAULT_CONTENT_ID,
                DEFAULT_CONTENT_TITLE
            );

            // then
            assertThat(session.getWatcherName()).isNull();
        }

        @Test
        @DisplayName("watcherProfileImagePath가 null이어도 생성 성공")
        void withNullWatcherProfileImagePath_createsWatchingSessionModel() {
            // when
            WatchingSessionModel session = WatchingSessionModel.create(
                DEFAULT_WATCHER_ID,
                DEFAULT_WATCHER_NAME,
                null,
                DEFAULT_CONTENT_ID,
                DEFAULT_CONTENT_TITLE
            );

            // then
            assertThat(session.getWatcherProfileImagePath()).isNull();
        }

        @Test
        @DisplayName("contentTitle이 null이어도 생성 성공")
        void withNullContentTitle_createsWatchingSessionModel() {
            // when
            WatchingSessionModel session = WatchingSessionModel.create(
                DEFAULT_WATCHER_ID,
                DEFAULT_WATCHER_NAME,
                DEFAULT_WATCHER_PROFILE_IMAGE_PATH,
                DEFAULT_CONTENT_ID,
                null
            );

            // then
            assertThat(session.getContentTitle()).isNull();
        }

        @Test
        @DisplayName("watcherId가 null이면 예외 발생")
        @SuppressWarnings("ConstantConditions")
        void withNullWatcherId_throwsException() {
            assertThatThrownBy(() -> WatchingSessionModel.create(
                null,
                DEFAULT_WATCHER_NAME,
                DEFAULT_WATCHER_PROFILE_IMAGE_PATH,
                DEFAULT_CONTENT_ID,
                DEFAULT_CONTENT_TITLE
            ))
                .isInstanceOf(InvalidWatchingSessionDataException.class);
        }

        @Test
        @DisplayName("contentId가 null이면 예외 발생")
        @SuppressWarnings("ConstantConditions")
        void withNullContentId_throwsException() {
            assertThatThrownBy(() -> WatchingSessionModel.create(
                DEFAULT_WATCHER_ID,
                DEFAULT_WATCHER_NAME,
                DEFAULT_WATCHER_PROFILE_IMAGE_PATH,
                null,
                DEFAULT_CONTENT_TITLE
            ))
                .isInstanceOf(InvalidWatchingSessionDataException.class);
        }
    }
}
