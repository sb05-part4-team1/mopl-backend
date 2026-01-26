package com.mopl.domain.model.notification;

import com.mopl.domain.exception.notification.InvalidNotificationDataException;
import com.mopl.domain.model.base.BaseModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
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
    private UUID receiverId;

    public static NotificationModel create(
        String title,
        String content,
        NotificationLevel level,
        UUID receiverId
    ) {
        validateTitle(title);
        validateContent(content);
        validateLevel(level);
        validateReceiverId(receiverId);

        return NotificationModel.builder()
            .title(title)
            .content(content)
            .level(level)
            .receiverId(receiverId)
            .build();
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw InvalidNotificationDataException.withDetailMessage("제목은 비어있을 수 없습니다.");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw InvalidNotificationDataException.withDetailMessage(
                "제목은 " + TITLE_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateContent(String content) {
        if (content != null && content.length() > CONTENT_MAX_LENGTH) {
            throw InvalidNotificationDataException.withDetailMessage(
                "내용은 " + CONTENT_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateLevel(NotificationLevel level) {
        if (level == null) {
            throw InvalidNotificationDataException.withDetailMessage("알림 레벨은 null일 수 없습니다.");
        }
    }

    private static void validateReceiverId(UUID receiverId) {
        if (receiverId == null) {
            throw InvalidNotificationDataException.withDetailMessage("수신자 ID는 null일 수 없습니다.");
        }
    }
}
