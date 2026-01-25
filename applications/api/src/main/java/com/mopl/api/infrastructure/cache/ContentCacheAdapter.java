package com.mopl.api.infrastructure.cache;

import com.mopl.domain.support.cache.CacheName;
import com.mopl.domain.support.cache.ContentCachePort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentCacheAdapter implements ContentCachePort {

    private final CacheManager cacheManager;

    @Override
    public void evict(UUID contentId) {
        Cache cache = cacheManager.getCache(CacheName.CONTENTS);
        if (cache != null) {
            cache.evict(contentId);
        }
    }
}
