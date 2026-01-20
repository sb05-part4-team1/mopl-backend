package com.mopl.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication(scanBasePackages = {
    "com.mopl.batch",
    "com.mopl.jpa",
    "com.mopl.storage",
    "com.mopl.external"
},
// [핵심] "스프링아, Redis 관련 자동 설정은 아예 켜지도 마!"라고 명령합니다.
        // 이걸 추가하면 RedisKeyValueAdapter 에러가 사라집니다.
        exclude = {
                RedisAutoConfiguration.class,
                RedisRepositoriesAutoConfiguration.class
        })
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    // [치트키] Redis가 없어서 징징거리는 스프링을 달래기 위한 '가짜(Dummy) Bean' 등록
    // 배치 서버에서는 실제로 이 Bean을 안 쓸 것이므로, 설정 없이 껍데기만 만들어도 됩니다.
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        return new RedisTemplate<String, Object>() {
            @Override
            public void afterPropertiesSet() {
                // 원래 여기서 ConnectionFactory 유무를 검사하는데,
                // 아무것도 안 하게 비워둬서 검사를 통과시킵니다. (Pass!)
            }
        };
    }
}
