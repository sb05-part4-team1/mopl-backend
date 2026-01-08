package com.mopl.domain.fixture;

import static com.mopl.domain.fixture.FixtureMonkeyConfig.fixtureMonkey;

import com.mopl.domain.model.content.ContentModel;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import net.jqwik.api.Arbitraries;

import java.util.List;
import java.util.Locale;

public final class ContentModelFixture {

    private ContentModelFixture() {
    }

    public static ArbitraryBuilder<ContentModel> builder() {
        return fixtureMonkey().giveMeBuilder(ContentModel.class)
            .setNull("updatedAt")
            .set("type", Arbitraries.of("MOVIE", "DRAMA", "ANIME", "DOCUMENTARY"))
            .set("thumbnailUrl", Arbitraries.strings().alpha().ofLength(10)
                .map(s -> "https://example.com/" + s.toLowerCase(Locale.ROOT) + ".jpg"))
            .set("tags", List.of());
    }

    public static ContentModel create() {
        return builder().sample();
    }
}
