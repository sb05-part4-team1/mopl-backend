package com.mopl.worker.config;

import net.javacrumbs.shedlock.core.LockProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShedLockConfig 단위 테스트")
class ShedLockConfigTest {

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Test
    @DisplayName("LockProvider 빈이 정상적으로 생성됨")
    void lockProvider_beanCreated() {
        // given
        ShedLockConfig config = new ShedLockConfig();

        // when
        LockProvider lockProvider = config.lockProvider(connectionFactory);

        // then
        assertThat(lockProvider).isNotNull();
    }

    @Test
    @DisplayName("ShedLockConfig 인스턴스가 정상적으로 생성됨")
    void shedLockConfig_created() {
        // when
        ShedLockConfig config = new ShedLockConfig();

        // then
        assertThat(config).isNotNull();
    }
}
