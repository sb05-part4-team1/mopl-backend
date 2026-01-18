package com.mopl.jpa.config;

import com.mopl.jpa.repository.watchingsession.RedisWatchingSessionRepository;
import com.mopl.jpa.repository.watchingsession.query.RedisWatchingSessionQueryRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(
    prefix = "mopl.watching-session.repository",
    name = "type",
    havingValue = "redis"
)
@Import({
    RedisWatchingSessionRepository.class,
    RedisWatchingSessionQueryRepositoryImpl.class
})
@RequiredArgsConstructor
public class WatchingSessionRedisRepositoryConfig {
    // Import로 Bean 등록만 담당
}
