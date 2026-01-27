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
    @DisplayName("put()")
    class PutTest {

        @Test
        @DisplayName("캐시 존재 시 키와 값 저장")
        void withExistingCache_putsValue() {
            // given
            UUID key = UUID.randomUUID();
            String value = "testValue";
            given(cacheManager.getCache(CacheName.CONTENTS)).willReturn(cache);

            // when
            cacheAdapter.put(CacheName.CONTENTS, key, value);

            // then
            then(cacheManager).should().getCache(CacheName.CONTENTS);
            then(cache).should().put(key, value);
        }

        @Test
        @DisplayName("캐시 미존재 시 put 미호출")
        void withNonExistingCache_doesNotPut() {
            // given
            UUID key = UUID.randomUUID();
            String value = "testValue";
            given(cacheManager.getCache(CacheName.CONTENTS)).willReturn(null);

            // when
            cacheAdapter.put(CacheName.CONTENTS, key, value);

            // then
            then(cacheManager).should().getCache(CacheName.CONTENTS);
            then(cache).should(never()).put(key, value);
        }

        @Test
        @DisplayName("다른 캐시 이름으로 정상 동작")
        void withDifferentCacheName_works() {
            // given
            UUID key = UUID.randomUUID();
            String value = "testValue";
            given(cacheManager.getCache(CacheName.PLAYLISTS)).willReturn(cache);

            // when
            cacheAdapter.put(CacheName.PLAYLISTS, key, value);

            // then
            then(cacheManager).should().getCache(CacheName.PLAYLISTS);
            then(cache).should().put(key, value);
        }
    }
}
