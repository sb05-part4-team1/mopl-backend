package com.mopl.domain.fixture;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FixtureMonkeyConfig {

    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
        .defaultNotNull(true)
        .build();

    public static FixtureMonkey fixtureMonkey() {
        return FIXTURE_MONKEY;
    }
}
