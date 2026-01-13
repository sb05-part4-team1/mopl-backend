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
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 오직 create() 메서드를 통해서만 유효한 객체를 만들도록 강제
public class PlaylistModel extends BaseUpdatableModel {

    public static final int TITLE_MAX_LENGTH = 255;
    public static final int DESCRIPTION_MAX_LENGTH = 10_000;

    private UserModel owner; // 플레이리스트 소유자
    private String title;
    private String description;

    // 생성자 대신 스태틱 팩토리로 생성 + 유효성 검사
    public static PlaylistModel create(
        UserModel owner,
        String title,
        String description
    ) {
        if (owner == null) {
            throw new InvalidPlaylistDataException("소유자 정보는 null일 수 없습니다.");
        }
        if (owner.getId() == null) {
            throw new InvalidPlaylistDataException("소유자 ID는 null일 수 없습니다.");
        }
        if (title == null) {
            throw new InvalidPlaylistDataException("플레이리스트 제목은 null일 수 없습니다.");
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
        // title이 null이 아니면 수정
        if (newTitle != null) {
            validateTitle(newTitle);
            this.title = newTitle;
        }

        // description이 null이 아니면 수정
        if (newDescription != null) {
            validateDescription(newDescription);
            this.description = newDescription;
        }

        return this;
    }

    private static void validateTitle(String title) {
        if (title.isBlank()) {
            throw new InvalidPlaylistDataException("플레이리스트 제목은 공백일 수 없습니다.");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new InvalidPlaylistDataException("플레이리스트 제목은 " + TITLE_MAX_LENGTH
                + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateDescription(String description) {
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new InvalidPlaylistDataException("플레이리스트 설명은 " + DESCRIPTION_MAX_LENGTH
                + "자를 초과할 수 없습니다.");
        }
    }

}
