package com.mopl.test.fixture;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

import java.util.List;

/**
 * Central factory for creating FixtureMonkey instances with different configurations.
 */
public final class FixtureMonkeyFactory {

    private FixtureMonkeyFactory() {
    }

    /**
     * Default FixtureMonkey using field reflection.
     * Suitable for most domain models and DTOs.
     */
    private static final FixtureMonkey DEFAULT = FixtureMonkey.builder()
        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
        .defaultNotNull(true)
        .build();

    /**
     * FixtureMonkey for builder-based classes (e.g., JPA entities with @SuperBuilder).
     * Uses a failover strategy: Builder -> Constructor -> Field reflection.
     */
    private static final FixtureMonkey BUILDER = FixtureMonkey.builder()
        .objectIntrospector(new FailoverIntrospector(List.of(
            BuilderArbitraryIntrospector.INSTANCE,
            ConstructorPropertiesArbitraryIntrospector.INSTANCE,
            FieldReflectionArbitraryIntrospector.INSTANCE
        )))
        .defaultNotNull(true)
        .build();

    /**
     * FixtureMonkey optimized for JPA entities.
     * Handles @SuperBuilder annotated entities properly.
     */
    private static final FixtureMonkey JPA_ENTITY = FixtureMonkey.builder()
        .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
        .defaultNotNull(true)
        .build();

    /**
     * Returns the default FixtureMonkey instance using field reflection.
     */
    public static FixtureMonkey defaultMonkey() {
        return DEFAULT;
    }

    /**
     * Returns a FixtureMonkey instance for builder-based classes.
     */
    public static FixtureMonkey builderMonkey() {
        return BUILDER;
    }

    /**
     * Returns a FixtureMonkey instance optimized for JPA entities.
     */
    public static FixtureMonkey jpaEntityMonkey() {
        return JPA_ENTITY;
    }
}
