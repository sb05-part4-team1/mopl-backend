package com.mopl.domain.service.cache;

import lombok.Getter;

import java.time.Duration;

@Getter
public enum CacheName {

    USERS("users", Duration.ofMinutes(30));

    private final String value;
    private final Duration ttl;

    CacheName(String value, Duration ttl) {
        this.value = value;
        this.ttl = ttl;
    }
}
