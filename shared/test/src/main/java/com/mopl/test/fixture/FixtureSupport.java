package com.mopl.test.fixture;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import net.jqwik.api.Arbitraries;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Utility methods for creating test fixtures.
 */
public final class FixtureSupport {

    private FixtureSupport() {
    }

    private static final FixtureMonkey MONKEY = FixtureMonkeyFactory.defaultMonkey();

    /**
     * Creates a single instance of the given type.
     */
    public static <T> T create(Class<T> type) {
        return MONKEY.giveMeOne(type);
    }

    /**
     * Creates a single instance with customization.
     */
    public static <T> T create(Class<T> type, Consumer<ArbitraryBuilder<T>> customizer) {
        ArbitraryBuilder<T> builder = MONKEY.giveMeBuilder(type);
        customizer.accept(builder);
        return builder.sample();
    }

    /**
     * Creates a list of instances of the given type.
     */
    public static <T> List<T> createList(Class<T> type, int size) {
        return MONKEY.giveMe(type, size);
    }

    /**
     * Creates a list of instances with customization.
     */
    public static <T> List<T> createList(Class<T> type, int size, Consumer<ArbitraryBuilder<T>> customizer) {
        ArbitraryBuilder<T> builder = MONKEY.giveMeBuilder(type);
        customizer.accept(builder);
        return builder.sampleList(size);
    }

    /**
     * Creates a builder for the given type.
     */
    public static <T> ArbitraryBuilder<T> builder(Class<T> type) {
        return MONKEY.giveMeBuilder(type);
    }

    /**
     * Generates a random UUID.
     */
    public static UUID randomUuid() {
        return UUID.randomUUID();
    }

    /**
     * Generates a random email address.
     */
    public static String randomEmail() {
        return Arbitraries.strings().alpha().ofLength(8).sample().toLowerCase() + "@test.com";
    }

    /**
     * Generates a random string of specified length.
     */
    public static String randomString(int length) {
        return Arbitraries.strings().alpha().ofLength(length).sample();
    }

    /**
     * Generates a random alphanumeric string.
     */
    public static String randomAlphanumeric(int length) {
        return Arbitraries.strings().alpha().numeric().ofLength(length).sample();
    }

    /**
     * Generates a random positive integer.
     */
    public static int randomPositiveInt(int max) {
        return Arbitraries.integers().between(1, max).sample();
    }

    /**
     * Generates a random positive long.
     */
    public static long randomPositiveLong(long max) {
        return Arbitraries.longs().between(1L, max).sample();
    }

    /**
     * Generates a random instant within the past days.
     */
    public static Instant randomPastInstant(int daysAgo) {
        int randomDays = Arbitraries.integers().between(0, daysAgo).sample();
        return Instant.now().minus(randomDays, ChronoUnit.DAYS);
    }

    /**
     * Generates a random instant within the future days.
     */
    public static Instant randomFutureInstant(int daysAhead) {
        int randomDays = Arbitraries.integers().between(1, daysAhead).sample();
        return Instant.now().plus(randomDays, ChronoUnit.DAYS);
    }

    /**
     * Generates a random double within range.
     */
    public static double randomDouble(double min, double max) {
        return Arbitraries.doubles().between(min, max).sample();
    }
}
