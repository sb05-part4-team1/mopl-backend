package com.mopl.api.infrastructure.cache;

import com.mopl.domain.support.cache.CacheName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentCacheAdapter 테스트")
class ContentCacheAdapterTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private ContentCacheAdapter contentCacheAdapter;

    @BeforeEach
    void setUp() {
        contentCacheAdapter = new ContentCacheAdapter(cacheManager);
    }

    @Nested
    @DisplayName("evict() - 캐시 삭제")
    class EvictTest {

        @Test
        @DisplayName("캐시가 존재하면 해당 contentId 캐시 삭제")
        void withExistingCache_evictsContentId() {
            // given
            UUID contentId = UUID.randomUUID();
            given(cacheManager.getCache(CacheName.CONTENTS)).willReturn(cache);

            // when
            contentCacheAdapter.evict(contentId);

            // then
            then(cacheManager).should().getCache(CacheName.CONTENTS);
            then(cache).should().evict(contentId);
        }

        @Test
        @DisplayName("캐시가 존재하지 않으면 evict 호출하지 않음")
        void withNonExistingCache_doesNotEvict() {
            // given
            UUID contentId = UUID.randomUUID();
            given(cacheManager.getCache(CacheName.CONTENTS)).willReturn(null);

            // when
            contentCacheAdapter.evict(contentId);

            // then
            then(cacheManager).should().getCache(CacheName.CONTENTS);
            then(cache).should(never()).evict(contentId);
        }
    }
}
