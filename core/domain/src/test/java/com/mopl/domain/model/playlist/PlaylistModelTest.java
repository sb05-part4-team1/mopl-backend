package com.mopl.domain.model.playlist;

import com.mopl.domain.exception.playlist.InvalidPlaylistDataException;
import com.mopl.domain.fixture.UserModelFixture;
import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static com.mopl.domain.model.playlist.PlaylistModel.DESCRIPTION_MAX_LENGTH;
import static com.mopl.domain.model.playlist.PlaylistModel.TITLE_MAX_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PlaylistModel 단위 테스트")
class PlaylistModelTest {

    private static final String DEFAULT_TITLE = "내 플레이리스트";
    private static final String DEFAULT_DESCRIPTION = "플레이리스트 설명";

    static Stream<Arguments> blankStringProvider() {
        return Stream.of(
            Arguments.of("null", null),
            Arguments.of("빈 문자열", ""),
            Arguments.of("공백만", "   ")
        );
    }

    private static PlaylistModel createDefaultPlaylist() {
        UserModel owner = UserModelFixture.create();
        return PlaylistModel.create(DEFAULT_TITLE, DEFAULT_DESCRIPTION, owner);
    }

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 PlaylistModel 생성")
        void withValidData_createsPlaylistModel() {
            // given
            UserModel owner = UserModelFixture.create();

            // when
            PlaylistModel playlist = PlaylistModel.create(DEFAULT_TITLE, DEFAULT_DESCRIPTION, owner);

            // then
            assertThat(playlist.getOwner()).isEqualTo(owner);
            assertThat(playlist.getTitle()).isEqualTo(DEFAULT_TITLE);
            assertThat(playlist.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        }

        @Test
        @DisplayName("설명이 null이어도 생성 성공")
        void withNullDescription_createsPlaylistModel() {
            // given
            UserModel owner = UserModelFixture.create();

            // when
            PlaylistModel playlist = PlaylistModel.create(DEFAULT_TITLE, null, owner);

            // then
            assertThat(playlist.getTitle()).isEqualTo(DEFAULT_TITLE);
            assertThat(playlist.getDescription()).isNull();
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.mopl.domain.model.playlist.PlaylistModelTest#blankStringProvider")
        @DisplayName("제목이 비어있으면 예외 발생")
        void withBlankTitle_throwsException(String description, String title) {
            UserModel owner = UserModelFixture.create();

            assertThatThrownBy(() -> PlaylistModel.create(title, DEFAULT_DESCRIPTION, owner))
                .isInstanceOf(InvalidPlaylistDataException.class);
        }

        @Test
        @DisplayName("제목이 정확히 최대 길이면 생성 성공")
        void withTitleAtMaxLength_createsPlaylistModel() {
            UserModel owner = UserModelFixture.create();
            String maxTitle = "가".repeat(TITLE_MAX_LENGTH);

            PlaylistModel playlist = PlaylistModel.create(maxTitle, DEFAULT_DESCRIPTION, owner);

            assertThat(playlist.getTitle()).isEqualTo(maxTitle);
        }

        @Test
        @DisplayName("제목이 최대 길이 초과하면 예외 발생")
        void withTitleExceedingMaxLength_throwsException() {
            UserModel owner = UserModelFixture.create();
            String longTitle = "가".repeat(TITLE_MAX_LENGTH + 1);

            assertThatThrownBy(() -> PlaylistModel.create(longTitle, DEFAULT_DESCRIPTION, owner))
                .isInstanceOf(InvalidPlaylistDataException.class);
        }

        // null은 허용되므로 빈 문자열과 공백만 테스트 (withNullDescription_createsPlaylistModel 참조)
        @ParameterizedTest
        @DisplayName("설명이 비어있으면 예외 발생")
        @ValueSource(strings = {"", "   "})
        void withBlankDescription_throwsException(String blankDescription) {
            UserModel owner = UserModelFixture.create();

            assertThatThrownBy(() -> PlaylistModel.create(DEFAULT_TITLE, blankDescription, owner))
                .isInstanceOf(InvalidPlaylistDataException.class);
        }

        @Test
        @DisplayName("설명이 정확히 최대 길이면 생성 성공")
        void withDescriptionAtMaxLength_createsPlaylistModel() {
            UserModel owner = UserModelFixture.create();
            String maxDescription = "가".repeat(DESCRIPTION_MAX_LENGTH);

            PlaylistModel playlist = PlaylistModel.create(DEFAULT_TITLE, maxDescription, owner);

            assertThat(playlist.getDescription()).isEqualTo(maxDescription);
        }

        @Test
        @DisplayName("설명이 최대 길이 초과하면 예외 발생")
        void withDescriptionExceedingMaxLength_throwsException() {
            UserModel owner = UserModelFixture.create();
            String longDescription = "가".repeat(DESCRIPTION_MAX_LENGTH + 1);

            assertThatThrownBy(() -> PlaylistModel.create(DEFAULT_TITLE, longDescription, owner))
                .isInstanceOf(InvalidPlaylistDataException.class);
        }

        @Test
        @DisplayName("소유자가 null이면 예외 발생")
        @SuppressWarnings("DataFlowIssue")
        void withNullOwner_throwsException() {
            assertThatThrownBy(() -> PlaylistModel.create(DEFAULT_TITLE, DEFAULT_DESCRIPTION, null))
                .isInstanceOf(InvalidPlaylistDataException.class);
        }

        @Test
        @DisplayName("소유자 ID가 null이면 예외 발생")
        void withNullOwnerId_throwsException() {
            UserModel ownerWithNullId = UserModel.builder().build();

            assertThatThrownBy(() -> PlaylistModel.create(DEFAULT_TITLE, DEFAULT_DESCRIPTION, ownerWithNullId))
                .isInstanceOf(InvalidPlaylistDataException.class);
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("유효한 데이터로 변경하면 새 객체 반환")
        void withValidData_returnsNewInstance() {
            // given
            PlaylistModel playlist = createDefaultPlaylist();
            String newTitle = "수정된 제목";
            String newDescription = "수정된 설명";

            // when
            PlaylistModel result = playlist.update(newTitle, newDescription);

            // then
            assertThat(result.getTitle()).isEqualTo(newTitle);
            assertThat(result.getDescription()).isEqualTo(newDescription);
            assertThat(result).isNotSameAs(playlist);
        }

        @Test
        @DisplayName("제목만 수정하면 제목만 변경")
        void withOnlyTitle_updatesTitleOnly() {
            // given
            PlaylistModel playlist = createDefaultPlaylist();
            String originalDescription = playlist.getDescription();
            String newTitle = "수정된 제목";

            // when
            PlaylistModel result = playlist.update(newTitle, null);

            // then
            assertThat(result.getTitle()).isEqualTo(newTitle);
            assertThat(result.getDescription()).isEqualTo(originalDescription);
            assertThat(result).isNotSameAs(playlist);
        }

        @Test
        @DisplayName("설명만 수정하면 설명만 변경")
        void withOnlyDescription_updatesDescriptionOnly() {
            // given
            PlaylistModel playlist = createDefaultPlaylist();
            String originalTitle = playlist.getTitle();
            String newDescription = "수정된 설명";

            // when
            PlaylistModel result = playlist.update(null, newDescription);

            // then
            assertThat(result.getTitle()).isEqualTo(originalTitle);
            assertThat(result.getDescription()).isEqualTo(newDescription);
            assertThat(result).isNotSameAs(playlist);
        }

        @Test
        @DisplayName("둘 다 null이면 값 유지하고 새 객체 반환")
        void withBothNull_returnsNewInstance() {
            // given
            PlaylistModel playlist = createDefaultPlaylist();
            String originalTitle = playlist.getTitle();
            String originalDescription = playlist.getDescription();

            // when
            PlaylistModel result = playlist.update(null, null);

            // then
            assertThat(result.getTitle()).isEqualTo(originalTitle);
            assertThat(result.getDescription()).isEqualTo(originalDescription);
            assertThat(result).isNotSameAs(playlist);
        }

        // null은 기존값 유지이므로 빈 문자열과 공백만 테스트 (withOnlyDescription_updatesDescriptionOnly 참조)
        @ParameterizedTest
        @DisplayName("제목이 비어있으면 예외 발생")
        @ValueSource(strings = {"", "   "})
        void withBlankTitle_throwsException(String blankTitle) {
            // given
            PlaylistModel playlist = createDefaultPlaylist();

            // when & then
            assertThatThrownBy(() -> playlist.update(blankTitle, null))
                .isInstanceOf(InvalidPlaylistDataException.class);
        }

        @Test
        @DisplayName("제목이 정확히 최대 길이면 수정 성공")
        void withTitleAtMaxLength_updatesTitle() {
            // given
            PlaylistModel playlist = createDefaultPlaylist();
            String maxTitle = "가".repeat(TITLE_MAX_LENGTH);

            // when
            PlaylistModel result = playlist.update(maxTitle, null);

            // then
            assertThat(result.getTitle()).isEqualTo(maxTitle);
        }

        @Test
        @DisplayName("제목이 최대 길이 초과하면 예외 발생")
        void withTitleExceedingMaxLength_throwsException() {
            // given
            PlaylistModel playlist = createDefaultPlaylist();
            String longTitle = "가".repeat(TITLE_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> playlist.update(longTitle, null))
                .isInstanceOf(InvalidPlaylistDataException.class);
        }

        // null은 기존값 유지이므로 빈 문자열과 공백만 테스트 (withOnlyTitle_updatesTitleOnly 참조)
        @ParameterizedTest
        @DisplayName("설명이 비어있으면 예외 발생")
        @ValueSource(strings = {"", "   "})
        void withBlankDescription_throwsException(String blankDescription) {
            // given
            PlaylistModel playlist = createDefaultPlaylist();

            // when & then
            assertThatThrownBy(() -> playlist.update(null, blankDescription))
                .isInstanceOf(InvalidPlaylistDataException.class);
        }

        @Test
        @DisplayName("설명이 정확히 최대 길이면 수정 성공")
        void withDescriptionAtMaxLength_updatesDescription() {
            // given
            PlaylistModel playlist = createDefaultPlaylist();
            String maxDescription = "가".repeat(DESCRIPTION_MAX_LENGTH);

            // when
            PlaylistModel result = playlist.update(null, maxDescription);

            // then
            assertThat(result.getDescription()).isEqualTo(maxDescription);
        }

        @Test
        @DisplayName("설명이 최대 길이 초과하면 예외 발생")
        void withDescriptionExceedingMaxLength_throwsException() {
            // given
            PlaylistModel playlist = createDefaultPlaylist();
            String longDescription = "가".repeat(DESCRIPTION_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> playlist.update(null, longDescription))
                .isInstanceOf(InvalidPlaylistDataException.class);
        }
    }

    @Nested
    @DisplayName("withSubscriberAdded()")
    class WithSubscriberAddedTest {

        @Test
        @DisplayName("구독자 추가 시 구독자 수 1 증가")
        void increasesSubscriberCountByOne() {
            // given
            PlaylistModel playlist = createDefaultPlaylist();
            int originalCount = playlist.getSubscriberCount();

            // when
            PlaylistModel result = playlist.withSubscriberAdded();

            // then
            assertThat(result.getSubscriberCount()).isEqualTo(originalCount + 1);
            assertThat(result).isNotSameAs(playlist);
        }

        @Test
        @DisplayName("구독자 0명에서 추가 시 1명이 됨")
        void fromZeroSubscribers_becomesOne() {
            // given
            PlaylistModel playlist = createDefaultPlaylist();
            assertThat(playlist.getSubscriberCount()).isZero();

            // when
            PlaylistModel result = playlist.withSubscriberAdded();

            // then
            assertThat(result.getSubscriberCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("기존 필드 값 유지")
        void preservesOtherFields() {
            // given
            PlaylistModel playlist = createDefaultPlaylist();

            // when
            PlaylistModel result = playlist.withSubscriberAdded();

            // then
            assertThat(result.getTitle()).isEqualTo(playlist.getTitle());
            assertThat(result.getDescription()).isEqualTo(playlist.getDescription());
            assertThat(result.getOwner()).isEqualTo(playlist.getOwner());
        }
    }

    @Nested
    @DisplayName("withSubscriberRemoved()")
    class WithSubscriberRemovedTest {

        @Test
        @DisplayName("구독자 제거 시 구독자 수 1 감소")
        void decreasesSubscriberCountByOne() {
            // given
            PlaylistModel playlist = createDefaultPlaylist().withSubscriberAdded().withSubscriberAdded();
            int originalCount = playlist.getSubscriberCount();

            // when
            PlaylistModel result = playlist.withSubscriberRemoved();

            // then
            assertThat(result.getSubscriberCount()).isEqualTo(originalCount - 1);
            assertThat(result).isNotSameAs(playlist);
        }

        @Test
        @DisplayName("구독자 1명에서 제거 시 0명이 됨")
        void fromOneSubscriber_becomesZero() {
            // given
            PlaylistModel playlist = createDefaultPlaylist().withSubscriberAdded();
            assertThat(playlist.getSubscriberCount()).isEqualTo(1);

            // when
            PlaylistModel result = playlist.withSubscriberRemoved();

            // then
            assertThat(result.getSubscriberCount()).isZero();
        }

        @Test
        @DisplayName("구독자 0명일 때 제거 시 예외 발생")
        void withZeroSubscribers_throwsException() {
            // given
            PlaylistModel playlist = createDefaultPlaylist();
            assertThat(playlist.getSubscriberCount()).isZero();

            // when & then
            assertThatThrownBy(playlist::withSubscriberRemoved)
                .isInstanceOf(InvalidPlaylistDataException.class);
        }

        @Test
        @DisplayName("기존 필드 값 유지")
        void preservesOtherFields() {
            // given
            PlaylistModel playlist = createDefaultPlaylist().withSubscriberAdded();

            // when
            PlaylistModel result = playlist.withSubscriberRemoved();

            // then
            assertThat(result.getTitle()).isEqualTo(playlist.getTitle());
            assertThat(result.getDescription()).isEqualTo(playlist.getDescription());
            assertThat(result.getOwner()).isEqualTo(playlist.getOwner());
        }
    }
}
