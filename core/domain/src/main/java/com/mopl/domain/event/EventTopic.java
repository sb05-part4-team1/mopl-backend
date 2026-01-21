package com.mopl.domain.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventTopic {

    public static final String USER_FOLLOWED = "mopl.user.followed";
    public static final String USER_UNFOLLOWED = "mopl.user.unfollowed";
    public static final String USER_ROLE_CHANGED = "mopl.user.role-changed";

    public static final String PLAYLIST_SUBSCRIBED = "mopl.playlist.subscribed";
    public static final String PLAYLIST_UNSUBSCRIBED = "mopl.playlist.unsubscribed";
    public static final String PLAYLIST_CONTENT_ADDED = "mopl.playlist.content-added";

    public static final String DIRECT_MESSAGE_RECEIVED = "mopl.conversation.direct-message.received";

    public static List<String> all() {
        return List.of(
            USER_FOLLOWED,
            USER_UNFOLLOWED,
            USER_ROLE_CHANGED,
            PLAYLIST_SUBSCRIBED,
            PLAYLIST_UNSUBSCRIBED,
            PLAYLIST_CONTENT_ADDED,
            DIRECT_MESSAGE_RECEIVED
        );
    }
}
