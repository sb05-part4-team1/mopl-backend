package com.mopl.domain.support.redis;

import java.util.UUID;

/**
 * WatchingSession Redis Key 규칙 중앙화.
 *
 * - 키 문자열(prefix/suffix)은 여기에서만 정의한다.
 * - 다른 클래스에서는 문자열 조합 금지하고 이 클래스 메서드만 사용한다.
 */
public final class WatchingSessionRedisKeys {

    private WatchingSessionRedisKeys() {
    }

    // ===== watcherCount(Set) =====
    private static final String WATCHING_COUNT_PREFIX = "watching_count:";

    // ===== session/model 저장 =====
    private static final String SESSION_KEY_PREFIX = "ws:session:";                 // ws:session:{sessionId}
    private static final String WATCHER_CURRENT_PREFIX = "ws:watcher:";             // ws:watcher:{watcherId}:current
    private static final String WATCHER_CURRENT_SUFFIX = ":current";
    private static final String CONTENT_SESSIONS_ZSET_PREFIX = "ws:content:";       // ws:content:{contentId}:sessions
    private static final String CONTENT_SESSIONS_ZSET_SUFFIX = ":sessions";

    /**
     * content별 시청자 수 Set 키
     * 예: watching_count:{contentId}
     */
    public static String watchingCountKey(UUID contentId) {
        return WATCHING_COUNT_PREFIX + contentId;
    }

    /**
     * sessionId -> WatchingSessionModel 저장 키
     * 예: ws:session:{sessionId}
     */
    public static String sessionKey(UUID sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }

    /**
     * watcherId -> current sessionId 저장 키
     * 예: ws:watcher:{watcherId}:current
     */
    public static String watcherCurrentKey(UUID watcherId) {
        return WATCHER_CURRENT_PREFIX + watcherId + WATCHER_CURRENT_SUFFIX;
    }

    /**
     * content별 sessionId 정렬(ZSET) 키 (createdAt score)
     * 예: ws:content:{contentId}:sessions
     */
    public static String contentSessionsZsetKey(UUID contentId) {
        return CONTENT_SESSIONS_ZSET_PREFIX + contentId + CONTENT_SESSIONS_ZSET_SUFFIX;
    }
}
