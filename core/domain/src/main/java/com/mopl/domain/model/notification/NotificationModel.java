package com.mopl.domain.model.notification;

import com.mopl.domain.model.base.BaseModel;
import com.mopl.domain.model.user.UserModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationModel extends BaseModel {

    public static final int TITLE_MAX_LENGTH = 500;
    public static final int CONTENT_MAX_LENGTH = 9999;
    public static final int LEVEL_MAX_LENGTH = 20;

    private String title;
    private String content;
    private NotificationLevel level;
    private UserModel receiver;

    public enum NotificationLevel {
        INFO, WARNING, ERROR
    }

    public static NotificationModel create(
        String title,
        String content,
        NotificationLevel level,
        UserModel receiver
    ) {
        return NotificationModel.builder()
            .title(title)
            .content(content)
            .level(level)
            .receiver(receiver)
            .build();
    }
}
