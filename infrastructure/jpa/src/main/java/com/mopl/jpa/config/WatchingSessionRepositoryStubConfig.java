package com.mopl.jpa.config;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.UUID;

@Configuration
@ConditionalOnProperty(
    prefix = "mopl.watching-session.repository",
    name = "type",
    havingValue = "stub",
    matchIfMissing = true // 설정 없으면 기본은 stub
)
public class WatchingSessionRepositoryStubConfig {

    /**
     * 다른 WatchingSessionRepository 구현체(예: Fake, Redis)가 이미 Bean으로 등록되어 있으면
     * 이 Stub Bean은 등록되지 않게 막는다.
     */
    @Bean
    @ConditionalOnMissingBean(WatchingSessionRepository.class)
    public WatchingSessionRepository watchingSessionRepository() {
        return new EmptyWatchingSessionRepository();
    }

    /**
     * Redis 붙기 전 임시 구현체: 항상 Optional.empty() 반환 -> Controller는 204 반환
     */
    static class EmptyWatchingSessionRepository implements WatchingSessionRepository {

        @Override
        public Optional<WatchingSessionModel> findByWatcherId(UUID watcherId) {
            return Optional.empty();
        }
    }
}
