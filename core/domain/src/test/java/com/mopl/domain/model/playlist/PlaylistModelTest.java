package com.mopl.domain.model.playlist;

import com.mopl.domain.exception.playlist.InvalidPlaylistDataException;
import com.mopl.domain.fixture.PlaylistModelFixture;
import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("PlaylistModel 단위 테스트")
class PlaylistModelTest {

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 플레이리스트를 생성한다")
        void withValidData_createsPlaylist() {
            // given
            UserModel owner = mock(UserModel.class);
            given(owner.getId()).willReturn(UUID.randomUUID());

            String title = "내 플레이리스트";
            String description = "플레이리스트 설명";

            // when
            PlaylistModel playlist = PlaylistModel.create(owner, title, description);

            // then
            assertThat(playlist.getOwner()).isEqualTo(owner);
            assertThat(playlist.getTitle()).isEqualTo(title);
            assertThat(playlist.getDescription()).isEqualTo(description);
        }

        @Test
        @DisplayName("설명이 null이어도 플레이리스트를 생성할 수 있다")
        void withNullDescription_createsPlaylist() {
            // given
            UserModel owner = mock(UserModel.class);
            given(owner.getId()).willReturn(UUID.randomUUID());

            String title = "내 플레이리스트";

            // when
            PlaylistModel playlist = PlaylistModel.create(owner, title, null);

            // then
            assertThat(playlist.getTitle()).isEqualTo(title);
            assertThat(playlist.getDescription()).isNull();
        }

        @Test
        @DisplayName("소유자가 null이면 예외가 발생한다")
        void withNullOwner_throwsException() {
            // when & then
            assertThatThrownBy(() -> PlaylistModel.create(null, "제목", "설명"))
                .isInstanceOf(InvalidPlaylistDataException.class)
                .satisfies(e -> assertThat(((InvalidPlaylistDataException) e).getDetails())
                    .containsEntry("reason", "소유자 정보는 null일 수 없습니다."));
        }

        @Test
        @DisplayName("소유자 ID가 null이면 예외가 발생한다")
        void withNullOwnerId_throwsException() {
            // given
            UserModel owner = mock(UserModel.class);
            given(owner.getId()).willReturn(null);

            // when & then
            assertThatThrownBy(() -> PlaylistModel.create(owner, "제목", "설명"))
                .isInstanceOf(InvalidPlaylistDataException.class)
                .satisfies(e -> assertThat(((InvalidPlaylistDataException) e).getDetails())
                    .containsEntry("reason", "소유자 정보는 null일 수 없습니다."));
        }

        @Test
        @DisplayName("제목이 null이면 예외가 발생한다")
        void withNullTitle_throwsException() {
            // given
            UserModel owner = mock(UserModel.class);
            given(owner.getId()).willReturn(UUID.randomUUID());

            // when & then
            assertThatThrownBy(() -> PlaylistModel.create(owner, null, "설명"))
                .isInstanceOf(InvalidPlaylistDataException.class)
                .satisfies(e -> assertThat(((InvalidPlaylistDataException) e).getDetails())
                    .containsEntry("reason", "제목은 null일 수 없습니다."));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   "})
        @DisplayName("제목이 공백이면 예외가 발생한다")
        void withBlankTitle_throwsException(String blankTitle) {
            // given
            UserModel owner = mock(UserModel.class);
            given(owner.getId()).willReturn(UUID.randomUUID());

            // when & then
            assertThatThrownBy(() -> PlaylistModel.create(owner, blankTitle, "설명"))
                .isInstanceOf(InvalidPlaylistDataException.class)
                .satisfies(e -> assertThat(((InvalidPlaylistDataException) e).getDetails())
                    .containsEntry("reason", "제목은 공백일 수 없습니다."));
        }

        @Test
        @DisplayName("제목이 최대 길이를 초과하면 예외가 발생한다")
        void withTooLongTitle_throwsException() {
            // given
            UserModel owner = mock(UserModel.class);
            given(owner.getId()).willReturn(UUID.randomUUID());
            String tooLongTitle = "a".repeat(PlaylistModel.TITLE_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> PlaylistModel.create(owner, tooLongTitle, "설명"))
                .isInstanceOf(InvalidPlaylistDataException.class)
                .satisfies(e -> assertThat(((InvalidPlaylistDataException) e).getDetails())
                    .containsEntry("reason",
                        "제목은 " + PlaylistModel.TITLE_MAX_LENGTH + "자를 초과할 수 없습니다."));
        }

        @Test
        @DisplayName("설명이 최대 길이를 초과하면 예외가 발생한다")
        void withTooLongDescription_throwsException() {
            // given
            UserModel owner = mock(UserModel.class);
            given(owner.getId()).willReturn(UUID.randomUUID());
            String tooLongDescription = "a".repeat(PlaylistModel.DESCRIPTION_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> PlaylistModel.create(owner, "제목", tooLongDescription))
                .isInstanceOf(InvalidPlaylistDataException.class)
                .satisfies(e -> assertThat(((InvalidPlaylistDataException) e).getDetails())
                    .containsEntry("reason",
                        "설명은 " + PlaylistModel.DESCRIPTION_MAX_LENGTH + "자를 초과할 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("유효한 제목과 설명으로 업데이트하면 값이 변경된다")
        void withValidData_updatesPlaylist() {
            // given
            PlaylistModel playlist = PlaylistModelFixture.create();
            String newTitle = "수정된 제목";
            String newDescription = "수정된 설명";

            // when
            PlaylistModel updated = playlist.update(newTitle, newDescription);

            // then
            assertThat(updated.getTitle()).isEqualTo(newTitle);
            assertThat(updated.getDescription()).isEqualTo(newDescription);
        }

        @Test
        @DisplayName("제목만 수정하면 제목만 변경되고 설명은 유지된다")
        void withOnlyTitle_updatesTitleOnly() {
            // given
            PlaylistModel playlist = PlaylistModelFixture.create();
            String originalDescription = playlist.getDescription();
            String newTitle = "수정된 제목";

            // when
            PlaylistModel updated = playlist.update(newTitle, null);

            // then
            assertThat(updated.getTitle()).isEqualTo(newTitle);
            assertThat(updated.getDescription()).isEqualTo(originalDescription);
        }

        @Test
        @DisplayName("설명만 수정하면 설명만 변경되고 제목은 유지된다")
        void withOnlyDescription_updatesDescriptionOnly() {
            // given
            PlaylistModel playlist = PlaylistModelFixture.create();
            String originalTitle = playlist.getTitle();
            String newDescription = "수정된 설명";

            // when
            PlaylistModel updated = playlist.update(null, newDescription);

            // then
            assertThat(updated.getTitle()).isEqualTo(originalTitle);
            assertThat(updated.getDescription()).isEqualTo(newDescription);
        }

        @Test
        @DisplayName("둘 다 null이면 아무것도 변경되지 않는다")
        void withBothNull_noChanges() {
            // given
            PlaylistModel playlist = PlaylistModelFixture.create();
            String originalTitle = playlist.getTitle();
            String originalDescription = playlist.getDescription();

            // when
            PlaylistModel updated = playlist.update(null, null);

            // then
            assertThat(updated.getTitle()).isEqualTo(originalTitle);
            assertThat(updated.getDescription()).isEqualTo(originalDescription);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   "})
        @DisplayName("수정할 제목이 공백이면 예외가 발생한다")
        void withBlankTitle_throwsException(String blankTitle) {
            // given
            PlaylistModel playlist = PlaylistModelFixture.create();

            // when & then
            assertThatThrownBy(() -> playlist.update(blankTitle, "설명"))
                .isInstanceOf(InvalidPlaylistDataException.class)
                .satisfies(e -> assertThat(((InvalidPlaylistDataException) e).getDetails())
                    .containsEntry("reason", "제목은 공백일 수 없습니다."));
        }

        @Test
        @DisplayName("수정할 제목이 최대 길이를 초과하면 예외가 발생한다")
        void withTooLongTitle_throwsException() {
            // given
            PlaylistModel playlist = PlaylistModelFixture.create();
            String tooLongTitle = "a".repeat(PlaylistModel.TITLE_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> playlist.update(tooLongTitle, "설명"))
                .isInstanceOf(InvalidPlaylistDataException.class)
                .satisfies(e -> assertThat(((InvalidPlaylistDataException) e).getDetails())
                    .containsEntry("reason",
                        "제목은 " + PlaylistModel.TITLE_MAX_LENGTH + "자를 초과할 수 없습니다."));
        }

        @Test
        @DisplayName("수정할 설명이 최대 길이를 초과하면 예외가 발생한다")
        void withTooLongDescription_throwsException() {
            // given
            PlaylistModel playlist = PlaylistModelFixture.create();
            String tooLongDescription = "a".repeat(PlaylistModel.DESCRIPTION_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> playlist.update("제목", tooLongDescription))
                .isInstanceOf(InvalidPlaylistDataException.class)
                .satisfies(e -> assertThat(((InvalidPlaylistDataException) e).getDetails())
                    .containsEntry("reason",
                        "설명은 " + PlaylistModel.DESCRIPTION_MAX_LENGTH + "자를 초과할 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("삭제 요청 시 deletedAt 필드가 설정된다")
        void deletesPlaylist() {
            // given
            PlaylistModel playlist = PlaylistModelFixture.create();

            // when
            playlist.delete();

            // then
            assertThat(playlist.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 삭제된 플레이리스트를 다시 삭제해도 멱등성이 보장된다")
        void deleteAlreadyDeletedPlaylist_isIdempotent() {
            // given
            PlaylistModel playlist = PlaylistModelFixture.create();
            playlist.delete();

            // when
            playlist.delete();

            // then
            assertThat(playlist.getDeletedAt()).isNotNull();
        }
    }
}
