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
@DisplayName("CacheAdapter 테스트")
class CacheAdapterTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private CacheAdapter cacheAdapter;

    @BeforeEach
    void setUp() {
        cacheAdapter = new CacheAdapter(cacheManager);
    }

    @Nested
    @DisplayName("evict()")
    class EvictTest {

        @Test
        @DisplayName("캐시가 존재하면 해당 키의 캐시를 삭제한다")
        void withExistingCache_evictsKey() {
            // given
            UUID key = UUID.randomUUID();
            given(cacheManager.getCache(CacheName.CONTENTS)).willReturn(cache);

            // when
            cacheAdapter.evict(CacheName.CONTENTS, key);

            // then
            then(cacheManager).should().getCache(CacheName.CONTENTS);
            then(cache).should().evict(key);
        }

        @Test
        @DisplayName("캐시가 존재하지 않으면 evict를 호출하지 않는다")
        void withNonExistingCache_doesNotEvict() {
            // given
            UUID key = UUID.randomUUID();
            given(cacheManager.getCache(CacheName.CONTENTS)).willReturn(null);

            // when
            cacheAdapter.evict(CacheName.CONTENTS, key);

            // then
            then(cacheManager).should().getCache(CacheName.CONTENTS);
            then(cache).should(never()).evict(key);
        }

        @Test
        @DisplayName("다른 캐시 이름으로도 동작한다")
        void withDifferentCacheName_works() {
            // given
            UUID key = UUID.randomUUID();
            given(cacheManager.getCache(CacheName.PLAYLISTS)).willReturn(cache);

            // when
            cacheAdapter.evict(CacheName.PLAYLISTS, key);

            // then
            then(cacheManager).should().getCache(CacheName.PLAYLISTS);
            then(cache).should().evict(key);
        }
    }
}
