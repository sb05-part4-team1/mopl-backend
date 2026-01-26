package com.mopl.domain.fixture;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

import com.mopl.domain.model.review.ReviewModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import net.jqwik.api.Arbitraries;

public final class ReviewModelFixture {

    private ReviewModelFixture() {
    }

    public static ArbitraryBuilder<ReviewModel> builder() {
        return fixtureMonkey().giveMeBuilder(ReviewModel.class)
            .setNull("updatedAt")
            .set("rating", Arbitraries.of(0, 1, 2, 3, 4, 5)
                .map(Integer::doubleValue))
            .set("content.reviewCount", Arbitraries.integers().between(1, 1000));
    }

    public static ReviewModel create() {
        return builder().sample();
    }
}
