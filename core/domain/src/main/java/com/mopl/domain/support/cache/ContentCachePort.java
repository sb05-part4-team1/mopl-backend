package com.mopl.domain.support.cache;

import java.util.UUID;

public interface ContentCachePort {

    void evict(UUID contentId);
}
