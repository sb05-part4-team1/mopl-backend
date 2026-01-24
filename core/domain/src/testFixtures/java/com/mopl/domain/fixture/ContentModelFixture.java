package com.mopl.domain.fixture;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.content.ContentModel.ContentType;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import net.jqwik.api.Arbitraries;

import java.util.Locale;

public final class ContentModelFixture {

    private ContentModelFixture() {
    }

    public static ArbitraryBuilder<ContentModel> builder() {
        return fixtureMonkey().giveMeBuilder(ContentModel.class)
            .setNull("updatedAt")
            .set("type", Arbitraries.of(ContentType.movie, ContentType.tvSeries, ContentType.sport))
            .set("thumbnailPath", Arbitraries.strings().alpha().ofLength(10)
                .map(s -> "contents/" + s.toLowerCase(Locale.ROOT) + ".jpg"))
            .set("reviewCount", Arbitraries.integers().between(0, 1000))
            .set("averageRating", Arbitraries.doubles().between(0.0, 5.0));
    }

    public static ContentModel create() {
        return builder().sample();
    }
}
