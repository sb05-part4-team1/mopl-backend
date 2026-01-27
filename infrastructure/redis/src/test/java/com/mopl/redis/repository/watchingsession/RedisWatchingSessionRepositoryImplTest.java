package com.mopl.redis.repository.watchingsession;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.redis.config.RedisProperties;
import com.mopl.redis.config.RedisProperties.WatchingSessionConfig;
import com.mopl.redis.support.WatchingSessionRedisKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisWatchingSessionRepositoryImpl 단위 테스트")
class RedisWatchingSessionRepositoryImplTest {

    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    private RedisWatchingSessionRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        RedisProperties redisProperties = new RedisProperties(new WatchingSessionConfig(DEFAULT_TTL));
        repository = new RedisWatchingSessionRepositoryImpl(redisTemplate, redisProperties);
    }

    @Nested
    @DisplayName("findByWatcherId()")
    class FindByWatcherIdTest {

        @Test
        @DisplayName("저장된 세션이 있으면 반환")
        void withExistingSession_returnsSession() {
            // given
            UUID watcherId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            String sessionKey = WatchingSessionRedisKeys.watcherSessionKey(watcherId);

            WatchingSessionModel model = WatchingSessionModel.builder()
                .watcherId(watcherId)
                .contentId(contentId)
                .createdAt(Instant.now())
                .build();

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(sessionKey)).willReturn(model);

            // when
            Optional<WatchingSessionModel> result = repository.findByWatcherId(watcherId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getWatcherId()).isEqualTo(watcherId);
            assertThat(result.get().getContentId()).isEqualTo(contentId);
        }

        @Test
        @DisplayName("저장된 세션이 없으면 빈 Optional 반환")
        void withNonExistingSession_returnsEmpty() {
            // given
            UUID watcherId = UUID.randomUUID();
            String sessionKey = WatchingSessionRedisKeys.watcherSessionKey(watcherId);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(sessionKey)).willReturn(null);

            // when
            Optional<WatchingSessionModel> result = repository.findByWatcherId(watcherId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("저장된 값이 WatchingSessionModel 타입이 아니면 빈 Optional 반환")
        void withWrongType_returnsEmpty() {
            // given
            UUID watcherId = UUID.randomUUID();
            String sessionKey = WatchingSessionRedisKeys.watcherSessionKey(watcherId);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(sessionKey)).willReturn("invalid type");

            // when
            Optional<WatchingSessionModel> result = repository.findByWatcherId(watcherId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByContentId()")
    class CountByContentIdTest {

        @Test
        @DisplayName("ZSet 카디널리티 반환")
        void withExistingContent_returnsCount() {
            // given
            UUID contentId = UUID.randomUUID();
            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(5L);

            // when
            long result = repository.countByContentId(contentId);

            // then
            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("ZSet이 없으면 0 반환")
        void withNonExistingContent_returnsZero() {
            // given
            UUID contentId = UUID.randomUUID();
            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(zsetKey)).willReturn(null);

            // when
            long result = repository.countByContentId(contentId);

            // then
            assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("countByContentIdIn()")
    class CountByContentIdInTest {

        @Test
        @DisplayName("빈 리스트면 빈 Map 반환")
        void withEmptyList_returnsEmptyMap() {
            // when
            Map<UUID, Long> result = repository.countByContentIdIn(List.of());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("여러 contentId에 대해 파이프라인으로 카운트 조회")
        void withMultipleContentIds_returnsCounts() {
            // given
            UUID contentId1 = UUID.randomUUID();
            UUID contentId2 = UUID.randomUUID();
            List<UUID> contentIds = List.of(contentId1, contentId2);

            List<Object> pipelineResults = List.of(10L, 20L);
            given(redisTemplate.executePipelined(any(RedisCallback.class))).willReturn(pipelineResults);

            // when
            Map<UUID, Long> result = repository.countByContentIdIn(contentIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(contentId1)).isEqualTo(10L);
            assertThat(result.get(contentId2)).isEqualTo(20L);
        }

        @Test
        @DisplayName("null 카운트는 0으로 처리")
        void withNullCount_treatsAsZero() {
            // given
            UUID contentId = UUID.randomUUID();
            List<UUID> contentIds = List.of(contentId);

            List<Object> pipelineResults = new ArrayList<>();
            pipelineResults.add(null);
            given(redisTemplate.executePipelined(any(RedisCallback.class))).willReturn(pipelineResults);

            // when
            Map<UUID, Long> result = repository.countByContentIdIn(contentIds);

            // then
            assertThat(result.get(contentId)).isZero();
        }

        @Test
        @DisplayName("파이프라인 내부에서 각 contentId에 대해 zCard 호출")
        @SuppressWarnings("unchecked")
        void verifyPipelineCallsZCardForEachContentId() {
            // given
            UUID contentId1 = UUID.randomUUID();
            UUID contentId2 = UUID.randomUUID();
            List<UUID> contentIds = List.of(contentId1, contentId2);

            RedisConnection connection = org.mockito.Mockito.mock(RedisConnection.class);
            RedisZSetCommands zSetCommands = org.mockito.Mockito.mock(RedisZSetCommands.class);
            given(connection.zSetCommands()).willReturn(zSetCommands);

            ArgumentCaptor<RedisCallback<Object>> callbackCaptor = ArgumentCaptor.forClass(RedisCallback.class);
            given(redisTemplate.executePipelined(callbackCaptor.capture())).willReturn(List.of(5L, 10L));

            // when
            repository.countByContentIdIn(contentIds);

            // then
            RedisCallback<Object> capturedCallback = callbackCaptor.getValue();
            capturedCallback.doInRedis(connection);

            String expectedKey1 = WatchingSessionRedisKeys.contentWatchersKey(contentId1);
            String expectedKey2 = WatchingSessionRedisKeys.contentWatchersKey(contentId2);

            then(zSetCommands).should().zCard(expectedKey1.getBytes());
            then(zSetCommands).should().zCard(expectedKey2.getBytes());
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("세션을 String과 ZSet에 TTL과 함께 저장")
        void withValidModel_savesToBothStructuresWithTtl() {
            // given
            UUID watcherId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            Instant createdAt = Instant.now();

            WatchingSessionModel model = WatchingSessionModel.builder()
                .watcherId(watcherId)
                .contentId(contentId)
                .createdAt(createdAt)
                .build();

            String sessionKey = WatchingSessionRedisKeys.watcherSessionKey(watcherId);
            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(redisTemplate.expire(zsetKey, DEFAULT_TTL)).willReturn(true);

            // when
            WatchingSessionModel result = repository.save(model);

            // then
            assertThat(result).isEqualTo(model);
            then(valueOperations).should().set(sessionKey, model, DEFAULT_TTL);
            then(zSetOperations).should().add(
                eq(zsetKey),
                eq(watcherId.toString()),
                eq((double) createdAt.toEpochMilli())
            );
            then(redisTemplate).should().expire(zsetKey, DEFAULT_TTL);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("null 모델이면 아무 작업 안 함")
        void withNullModel_doesNothing() {
            // when
            repository.delete(null);

            // then
            then(redisTemplate).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("watcherId가 null이면 아무 작업 안 함")
        void withNullWatcherId_doesNothing() {
            // given
            WatchingSessionModel model = WatchingSessionModel.builder()
                .watcherId(null)
                .build();

            // when
            repository.delete(model);

            // then
            then(redisTemplate).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("contentId가 있으면 String과 ZSet 모두에서 삭제")
        void withContentId_deletesFromBothStructures() {
            // given
            UUID watcherId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            WatchingSessionModel model = WatchingSessionModel.builder()
                .watcherId(watcherId)
                .contentId(contentId)
                .build();

            String sessionKey = WatchingSessionRedisKeys.watcherSessionKey(watcherId);
            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);

            // when
            repository.delete(model);

            // then
            then(redisTemplate).should().delete(sessionKey);
            then(zSetOperations).should().remove(zsetKey, watcherId.toString());
        }

        @Test
        @DisplayName("contentId가 null이면 저장된 세션에서 조회 후 삭제")
        void withNullContentId_looksUpFromRedis() {
            // given
            UUID watcherId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();

            WatchingSessionModel inputModel = WatchingSessionModel.builder()
                .watcherId(watcherId)
                .contentId(null)
                .build();

            WatchingSessionModel storedModel = WatchingSessionModel.builder()
                .watcherId(watcherId)
                .contentId(contentId)
                .build();

            String sessionKey = WatchingSessionRedisKeys.watcherSessionKey(watcherId);
            String zsetKey = WatchingSessionRedisKeys.contentWatchersKey(contentId);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(sessionKey)).willReturn(storedModel);
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);

            // when
            repository.delete(inputModel);

            // then
            then(redisTemplate).should().delete(sessionKey);
            then(zSetOperations).should().remove(zsetKey, watcherId.toString());
        }

        @Test
        @DisplayName("contentId가 null이고 저장된 세션도 없으면 String만 삭제")
        void withNullContentIdAndNoStoredSession_deletesOnlyString() {
            // given
            UUID watcherId = UUID.randomUUID();

            WatchingSessionModel model = WatchingSessionModel.builder()
                .watcherId(watcherId)
                .contentId(null)
                .build();

            String sessionKey = WatchingSessionRedisKeys.watcherSessionKey(watcherId);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(sessionKey)).willReturn(null);

            // when
            repository.delete(model);

            // then
            then(redisTemplate).should().delete(sessionKey);
            then(zSetOperations).shouldHaveNoInteractions();
        }
    }
}
