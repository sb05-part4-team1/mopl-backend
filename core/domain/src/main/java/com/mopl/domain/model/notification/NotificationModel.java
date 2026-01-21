package com.mopl.domain.model.notification;

import com.mopl.domain.exception.notification.InvalidNotificationDataException;
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

    public enum NotificationLevel {
        INFO, WARNING, ERROR
    }

    private String title;
    private String content;
    private NotificationLevel level;
    private UserModel receiver;

    public static NotificationModel create(
        String title,
        String content,
        NotificationLevel level,
        UserModel receiver
    ) {
        validateTitle(title);
        validateContent(content);
        validateLevel(level);
        validateReceiver(receiver);

        return NotificationModel.builder()
            .title(title)
            .content(content)
            .level(level)
            .receiver(receiver)
            .build();
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new InvalidNotificationDataException("제목은 비어있을 수 없습니다.");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new InvalidNotificationDataException(
                "제목은 " + TITLE_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateContent(String content) {
        if (content != null && content.length() > CONTENT_MAX_LENGTH) {
            throw new InvalidNotificationDataException(
                "내용은 " + CONTENT_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateLevel(NotificationLevel level) {
        if (level == null) {
            throw new InvalidNotificationDataException("알림 레벨은 null일 수 없습니다.");
        }
    }

    private static void validateReceiver(UserModel receiver) {
        if (receiver == null) {
            throw new InvalidNotificationDataException("수신자는 null일 수 없습니다.");
        }
    }
}
