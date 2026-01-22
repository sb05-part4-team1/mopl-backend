package com.mopl.batch.collect.tsdb.support;

import com.mopl.external.tsdb.model.EventItem;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TsdbEventTagResolver {

    public List<String> resolve(EventItem item) {
        if (item == null) {
            return List.of();
        }

        return Stream.of(
            item.strSport(),
            item.strHomeTeam(),
            item.strAwayTeam()
        )
            .filter(Objects::nonNull)
            .map(String::strip)
            .filter(s -> !s.isBlank())
            .distinct()
            .toList();
    }
}
