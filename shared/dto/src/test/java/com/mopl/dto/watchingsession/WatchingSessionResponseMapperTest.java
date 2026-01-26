package com.mopl.dto.watchingsession;

import com.mopl.domain.fixture.WatchingSessionModelFixture;
import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.storage.provider.StorageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("WatchingSessionResponseMapper 단위 테스트")
class WatchingSessionResponseMapperTest {

    @Mock
    private StorageProvider storageProvider;

    @InjectMocks
    private WatchingSessionResponseMapper mapper;

    @Nested
    @DisplayName("toDto()")
    class ToDtoTest {

        @Test
        @DisplayName("WatchingSessionModel을 WatchingSessionResponse로 변환")
        void withWatchingSessionModel_returnsWatchingSessionResponse() {
            // given
            WatchingSessionModel model = WatchingSessionModelFixture.create();
            String expectedProfileUrl = "https://cdn.example.com/" + model.getWatcherProfileImagePath();

            given(storageProvider.getUrl(model.getWatcherProfileImagePath())).willReturn(expectedProfileUrl);

            // when
            WatchingSessionResponse result = mapper.toDto(model);

            // then
            assertThat(result.id()).isEqualTo(model.getWatcherId());
            assertThat(result.createdAt()).isEqualTo(model.getCreatedAt());
            assertThat(result.watcher().userId()).isEqualTo(model.getWatcherId());
            assertThat(result.watcher().name()).isEqualTo(model.getWatcherName());
            assertThat(result.watcher().profileImageUrl()).isEqualTo(expectedProfileUrl);
            assertThat(result.content().id()).isEqualTo(model.getContentId());
            assertThat(result.content().title()).isEqualTo(model.getContentTitle());
        }

        @Test
        @DisplayName("프로필 이미지 경로가 null인 경우 null URL 반환")
        void withNullProfileImagePath_returnsNullProfileImageUrl() {
            // given
            WatchingSessionModel model = WatchingSessionModelFixture.builder()
                .setNull("watcherProfileImagePath")
                .sample();

            given(storageProvider.getUrl(null)).willReturn(null);

            // when
            WatchingSessionResponse result = mapper.toDto(model);

            // then
            assertThat(result.watcher().profileImageUrl()).isNull();
        }

        @Test
        @DisplayName("콘텐츠 관련 필드가 기본값으로 설정됨")
        void withWatchingSessionModel_setsDefaultContentFields() {
            // given
            WatchingSessionModel model = WatchingSessionModelFixture.create();
            String expectedProfileUrl = "https://cdn.example.com/profile.jpg";

            given(storageProvider.getUrl(model.getWatcherProfileImagePath())).willReturn(expectedProfileUrl);

            // when
            WatchingSessionResponse result = mapper.toDto(model);

            // then
            assertThat(result.content().type()).isNull();
            assertThat(result.content().description()).isNull();
            assertThat(result.content().thumbnailUrl()).isNull();
            assertThat(result.content().tags()).isEmpty();
            assertThat(result.content().averageRating()).isZero();
            assertThat(result.content().reviewCount()).isZero();
        }
    }
}
