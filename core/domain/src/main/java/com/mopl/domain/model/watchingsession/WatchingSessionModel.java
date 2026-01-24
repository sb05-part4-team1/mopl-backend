package com.mopl.domain.model.watchingsession;

import com.mopl.domain.exception.watchingsession.InvalidWatchingSessionDataException;
import com.mopl.domain.model.base.BaseModel;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WatchingSessionModel extends BaseModel {

    private UserModel watcher;
    private ContentModel content;

    public static WatchingSessionModel create(
        UserModel watcher,
        ContentModel content
    ) {
        if (watcher == null) {
            throw InvalidWatchingSessionDataException.withDetailMessage("시청자 정보는 null일 수 없습니다.");
        }
        if (watcher.getId() == null) {
            throw InvalidWatchingSessionDataException.withDetailMessage("시청자 ID는 null일 수 없습니다.");
        }
        if (content == null) {
            throw InvalidWatchingSessionDataException.withDetailMessage("콘텐츠 정보는 null일 수 없습니다.");
        }
        if (content.getId() == null) {
            throw InvalidWatchingSessionDataException.withDetailMessage("콘텐츠 ID는 null일 수 없습니다.");
        }

        return WatchingSessionModel.builder()
            .watcher(watcher)
            .content(content)
            .build();
    }
}
