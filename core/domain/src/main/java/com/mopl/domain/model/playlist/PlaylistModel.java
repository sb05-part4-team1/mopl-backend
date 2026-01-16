package com.mopl.domain.model.playlist;

import com.mopl.domain.exception.playlist.InvalidPlaylistDataException;
import com.mopl.domain.model.base.BaseUpdatableModel;
import com.mopl.domain.model.user.UserModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaylistModel extends BaseUpdatableModel {

    public static final int TITLE_MAX_LENGTH = 255;
    public static final int DESCRIPTION_MAX_LENGTH = 10_000;

    private UserModel owner;
    private String title;
    private String description;

    public static PlaylistModel create(
        UserModel owner,
        String title,
        String description
    ) {
        if (owner == null || owner.getId() == null) {
            throw new InvalidPlaylistDataException("소유자 정보는 null일 수 없습니다.");
        }
        if (title == null) {
            throw new InvalidPlaylistDataException("제목은 null일 수 없습니다.");
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
        if (newTitle != null) {
            validateTitle(newTitle);
            this.title = newTitle;
        }

        if (newDescription != null) {
            validateDescription(newDescription);
            this.description = newDescription;
        }

        return this;
    }

    private static void validateTitle(String title) {
        if (title.isBlank()) {
            throw new InvalidPlaylistDataException("제목은 공백일 수 없습니다.");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new InvalidPlaylistDataException("제목은 " + TITLE_MAX_LENGTH
                + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateDescription(String description) {
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new InvalidPlaylistDataException("설명은 " + DESCRIPTION_MAX_LENGTH
                + "자를 초과할 수 없습니다.");
        }
    }
}
