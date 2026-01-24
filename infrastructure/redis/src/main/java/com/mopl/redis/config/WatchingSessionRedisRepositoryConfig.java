package com.mopl.redis.config;

import com.mopl.redis.repository.watchingsession.RedisWatchingSessionRepositoryImpl;
import com.mopl.redis.repository.watchingsession.query.RedisWatchingSessionQueryRepositoryImpl;
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
    RedisWatchingSessionRepositoryImpl.class,
    RedisWatchingSessionQueryRepositoryImpl.class
})
public class WatchingSessionRedisRepositoryConfig {
}
