package com.mopl.domain.support.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheName {

    public static final String USERS = "users";
    public static final String USERS_BY_EMAIL = "users-by-email";
}
