package com.mopl.redis.repository.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static com.mopl.redis.repository.user.RedisTemporaryPasswordRepository.KEY_PREFIX;
import static com.mopl.redis.repository.user.RedisTemporaryPasswordRepository.TTL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("RedisTemporaryPasswordRepository 단위 테스트")
@ExtendWith(MockitoExtension.class)
class RedisTemporaryPasswordRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisTemporaryPasswordRepository repository;

    @DisplayName("findByEmail()")
    @Nested
    class FindByEmailTest {

        @DisplayName("저장된 비밀번호가 있으면 반환")
        @Test
        void withExistingPassword_returnsPassword() {
            // given
            String email = "test@example.com";
            String encodedPassword = "encodedPassword123";

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(KEY_PREFIX + email)).willReturn(encodedPassword);

            // when
            Optional<String> result = repository.findByEmail(email);

            // then
            assertThat(result).hasValue(encodedPassword);
        }

        @DisplayName("저장된 비밀번호가 없으면 빈 Optional 반환")
        @Test
        void withNonExistingPassword_returnsEmpty() {
            // given
            String email = "nonexistent@example.com";

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(KEY_PREFIX + email)).willReturn(null);

            // when
            Optional<String> result = repository.findByEmail(email);

            // then
            assertThat(result).isEmpty();
        }
    }

    @DisplayName("save()")
    @Nested
    class SaveTest {

        @DisplayName("이메일과 비밀번호를 TTL과 함께 저장")
        @Test
        void withEmailAndPassword_savesWithTTL() {
            // given
            String email = "test@example.com";
            String encodedPassword = "encodedPassword123";

            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            // when
            repository.save(email, encodedPassword);

            // then
            then(valueOperations).should().set(KEY_PREFIX + email, encodedPassword, TTL);
        }
    }

    @DisplayName("deleteByEmail()")
    @Nested
    class DeleteByEmailTest {

        @DisplayName("이메일로 임시 비밀번호 삭제")
        @Test
        void withEmail_deletesPassword() {
            // given
            String email = "test@example.com";

            // when
            repository.deleteByEmail(email);

            // then
            then(redisTemplate).should().delete(KEY_PREFIX + email);
        }
    }
}
