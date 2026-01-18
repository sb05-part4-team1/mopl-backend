package com.mopl.redis.repository.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisTemporaryPasswordRepository 단위 테스트")
class RedisTemporaryPasswordRepositoryTest {

    private static final String KEY_PREFIX = "user:temp-password:";
    private static final Duration TTL = Duration.ofMinutes(3);

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private RedisTemporaryPasswordRepository repository;

    @BeforeEach
    void setUp() {
        repository = new RedisTemporaryPasswordRepository(redisTemplate);
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("이메일과 인코딩된 비밀번호를 3분 TTL로 저장한다")
        void savesWithCorrectKeyAndTTL() {
            // given
            String email = "test@example.com";
            String encodedPassword = "encodedPassword123";
            String expectedKey = KEY_PREFIX + email;

            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            // when
            repository.save(email, encodedPassword);

            // then
            then(valueOperations).should().set(expectedKey, encodedPassword, TTL);
        }

        @Test
        @DisplayName("키가 user:temp-password: 접두사를 갖는다")
        void keyHasCorrectPrefix() {
            // given
            String email = "user@mopl.com";
            String encodedPassword = "encoded";

            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            // when
            repository.save(email, encodedPassword);

            // then
            then(valueOperations).should().set(
                org.mockito.ArgumentMatchers.startsWith(KEY_PREFIX),
                org.mockito.ArgumentMatchers.eq(encodedPassword),
                org.mockito.ArgumentMatchers.eq(TTL)
            );
        }
    }

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmailTest {

        @Test
        @DisplayName("저장된 비밀번호가 있으면 Optional로 반환한다")
        void whenPasswordExists_returnsOptionalWithValue() {
            // given
            String email = "test@example.com";
            String encodedPassword = "encodedPassword123";
            String key = KEY_PREFIX + email;

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(key)).willReturn(encodedPassword);

            // when
            Optional<String> result = repository.findByEmail(email);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(encodedPassword);
        }

        @Test
        @DisplayName("저장된 비밀번호가 없으면 빈 Optional을 반환한다")
        void whenPasswordNotExists_returnsEmptyOptional() {
            // given
            String email = "nonexistent@example.com";
            String key = KEY_PREFIX + email;

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(key)).willReturn(null);

            // when
            Optional<String> result = repository.findByEmail(email);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByEmail()")
    class DeleteByEmailTest {

        @Test
        @DisplayName("이메일로 저장된 임시 비밀번호를 삭제한다")
        void deletesPasswordByEmail() {
            // given
            String email = "test@example.com";
            String expectedKey = KEY_PREFIX + email;

            // when
            repository.deleteByEmail(email);

            // then
            then(redisTemplate).should().delete(expectedKey);
        }
    }
}
