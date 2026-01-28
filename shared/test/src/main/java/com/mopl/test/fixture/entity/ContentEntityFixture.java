package com.mopl.test.fixture.entity;

import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.test.fixture.FixtureMonkeyFactory;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import net.jqwik.api.Arbitraries;

/**
 * Test fixture for ContentEntity.
 */
public final class ContentEntityFixture {

    private ContentEntityFixture() {
    }

    public static ArbitraryBuilder<ContentEntity> builder() {
        return FixtureMonkeyFactory.jpaEntityMonkey().giveMeBuilder(ContentEntity.class)
            .setNull("id")
            .setNull("createdAt")
            .setNull("updatedAt")
            .setNull("deletedAt")
            .set("type", ContentType.movie)
            .set("title", Arbitraries.strings().alpha().ofLength(10))
            .set("description", Arbitraries.strings().alpha().ofLength(50))
            .set("thumbnailPath", "/thumbnails/test.jpg")
            .set("reviewCount", 0)
            .set("averageRating", 0.0)
            .set("popularityScore", 0.0);
    }

    public static ContentEntity create() {
        return builder().sample();
    }

    public static ContentEntity createMovie() {
        return builder()
            .set("type", ContentType.movie)
            .sample();
    }

    public static ContentEntity createTvSeries() {
        return builder()
            .set("type", ContentType.tvSeries)
            .sample();
    }

    public static ContentEntity createWithTitle(String title) {
        return builder()
            .set("title", title)
            .sample();
    }
}
