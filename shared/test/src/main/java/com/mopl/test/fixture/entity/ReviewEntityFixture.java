package com.mopl.test.fixture.entity;

import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.review.ReviewEntity;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.test.fixture.FixtureMonkeyFactory;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import net.jqwik.api.Arbitraries;

/**
 * Test fixture for ReviewEntity.
 */
public final class ReviewEntityFixture {

    private ReviewEntityFixture() {
    }

    public static ArbitraryBuilder<ReviewEntity> builder() {
        return FixtureMonkeyFactory.jpaEntityMonkey().giveMeBuilder(ReviewEntity.class)
            .setNull("id")
            .setNull("createdAt")
            .setNull("updatedAt")
            .setNull("deletedAt")
            .set("text", Arbitraries.strings().alpha().ofLength(100))
            .set("rating", Arbitraries.doubles().between(1.0, 5.0));
    }

    public static ReviewEntity create(ContentEntity content, UserEntity author) {
        return builder()
            .set("content", content)
            .set("author", author)
            .sample();
    }

    public static ReviewEntity createWithRating(ContentEntity content, UserEntity author, double rating) {
        return builder()
            .set("content", content)
            .set("author", author)
            .set("rating", rating)
            .sample();
    }
}
