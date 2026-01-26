package com.mopl.domain.support.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheName {

    public static final String USERS = "users";
    public static final String USERS_BY_EMAIL = "users-by-email";

    public static final String CONTENTS = "contents";
    public static final String CONTENT_TAGS = "content-tags";

    public static final String PLAYLISTS = "playlists";
    public static final String PLAYLIST_CONTENTS = "playlist-contents";

    public static final String PRESIGNED_URLS = "presigned-urls";

    public static String[] all() {
        return new String[]{
            USERS,
            USERS_BY_EMAIL,
            CONTENTS,
            CONTENT_TAGS,
            PLAYLISTS,
            PLAYLIST_CONTENTS,
            PRESIGNED_URLS
        };
    }
}
