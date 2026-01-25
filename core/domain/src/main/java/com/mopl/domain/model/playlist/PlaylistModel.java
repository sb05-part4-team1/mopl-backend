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
    private int subscriberCount;

    public static PlaylistModel create(
        String title,
        String description,
        UserModel owner
    ) {
        if (title == null) {
            throw InvalidPlaylistDataException.withDetailMessage("제목은 null일 수 없습니다.");
        }
        if (owner == null) {
            throw InvalidPlaylistDataException.withDetailMessage("소유자는 null일 수 없습니다.");
        }
        if (owner.getId() == null) {
            throw InvalidPlaylistDataException.withDetailMessage("소유자 ID는 null일 수 없습니다.");
        }

        validateTitle(title);
        if (description != null) {
            validateDescription(description);
        }

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

    public PlaylistModel withSubscriberAdded() {
        return this.toBuilder()
            .subscriberCount(this.subscriberCount + 1)
            .build();
    }

    public PlaylistModel withSubscriberRemoved() {
        if (this.subscriberCount <= 0) {
            throw InvalidPlaylistDataException.withDetailMessage("구독자가 없는 플레이리스트의 구독자를 제거할 수 없습니다.");
        }
        return this.toBuilder()
            .subscriberCount(this.subscriberCount - 1)
            .build();
    }

    private static void validateTitle(String title) {
        if (title.isBlank()) {
            throw InvalidPlaylistDataException.withDetailMessage("제목은 비어있을 수 없습니다.");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw InvalidPlaylistDataException.withDetailMessage(
                "제목은 " + TITLE_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateDescription(String description) {
        if (description.isBlank()) {
            throw InvalidPlaylistDataException.withDetailMessage("설명은 비어있을 수 없습니다.");
        }
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw InvalidPlaylistDataException.withDetailMessage(
                "설명은 " + DESCRIPTION_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }
}
