package com.mopl.redis.support;

import java.util.UUID;

/**
 * 키 구조:
 * - ws:content:{contentId}:watchers (ZSet) - member: watcherId, score: joinedAt
 * - ws:watcher:{watcherId}:session (String) - value: WatchingSessionModel
 */
public final class WatchingSessionRedisKeys {

    private WatchingSessionRedisKeys() {
    }

    private static final String CONTENT_WATCHERS_PREFIX = "ws:content:";
    private static final String CONTENT_WATCHERS_SUFFIX = ":watchers";
    private static final String WATCHER_SESSION_PREFIX = "ws:watcher:";
    private static final String WATCHER_SESSION_SUFFIX = ":session";

    /**
     * content별 시청자 ZSet 키 (member: watcherId, score: joinedAt)
     * 예: ws:content:{contentId}:watchers
     */
    public static String contentWatchersKey(UUID contentId) {
        return CONTENT_WATCHERS_PREFIX + contentId + CONTENT_WATCHERS_SUFFIX;
    }

    /**
     * watcher별 현재 세션 저장 키
     * 예: ws:watcher:{watcherId}:session
     */
    public static String watcherSessionKey(UUID watcherId) {
        return WATCHER_SESSION_PREFIX + watcherId + WATCHER_SESSION_SUFFIX;
    }
}
