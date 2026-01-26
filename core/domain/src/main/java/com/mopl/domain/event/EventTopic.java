package com.mopl.domain.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventTopic {

    public static final String USER_FOLLOWED = "mopl.user.followed";
    public static final String USER_UNFOLLOWED = "mopl.user.unfollowed";
    public static final String USER_ROLE_CHANGED = "mopl.user.role-changed";

    public static final String PLAYLIST_CREATED = "mopl.playlist.created";
    public static final String PLAYLIST_UPDATED = "mopl.playlist.updated";
    public static final String PLAYLIST_SUBSCRIBED = "mopl.playlist.subscribed";
    public static final String PLAYLIST_CONTENT_ADDED = "mopl.playlist.content-added";

    public static final String DIRECT_MESSAGE_SENT = "mopl.direct-message.sent";
}
