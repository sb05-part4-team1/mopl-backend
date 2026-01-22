package com.mopl.domain.model.playlist;

import com.mopl.domain.exception.playlist.InvalidPlaylistDataException;
import com.mopl.domain.model.base.BaseUpdatableModel;
import com.mopl.domain.model.user.UserModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public class PlaylistModel extends BaseUpdatableModel {

    public static final int TITLE_MAX_LENGTH = 255;
    public static final int DESCRIPTION_MAX_LENGTH = 10_000;

    private String title;
    private String description;
    private UserModel owner;

    public static PlaylistModel create(
        UserModel owner,
        String title,
        String description
    ) {
        if (owner == null) {
            throw InvalidPlaylistDataException.withDetailMessage("소유자는 null일 수 없습니다.");
        }
        if (owner.getId() == null) {
            throw InvalidPlaylistDataException.withDetailMessage("소유자 id는 null일 수 없습니다.");
        }
        if (title == null || title.isBlank()) {
            throw InvalidPlaylistDataException.withDetailMessage("제목은 비어있을 수 없습니다.");
        }

        validateTitle(title);
        validateDescription(description);

        return PlaylistModel.builder()
            .owner(owner)
            .title(title)
            .description(description)
            .build();
    }

    public PlaylistModel update(
        String newTitle,
        String newDescription
    ) {
        String updatedTitle = this.title;
        String updatedDescription = this.description;

        if (newTitle != null) {
            validateTitle(newTitle);
            updatedTitle = newTitle;
        }

        if (newDescription != null) {
            validateDescription(newDescription);
            updatedDescription = newDescription;
        }

        return this.toBuilder()
            .title(updatedTitle)
            .description(updatedDescription)
            .build();
    }

    private static void validateTitle(String title) {
        if (title.length() > TITLE_MAX_LENGTH) {
            throw InvalidPlaylistDataException.withDetailMessage(
                "제목은 " + TITLE_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateDescription(String description) {
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw InvalidPlaylistDataException.withDetailMessage(
                "설명은 " + DESCRIPTION_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }
}
