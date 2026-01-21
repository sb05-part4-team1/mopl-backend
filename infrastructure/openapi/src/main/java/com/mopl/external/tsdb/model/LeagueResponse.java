package com.mopl.external.tsdb.model;

import java.util.List;

public record LeagueResponse(
    List<LeagueItem> leagues
) {
}
