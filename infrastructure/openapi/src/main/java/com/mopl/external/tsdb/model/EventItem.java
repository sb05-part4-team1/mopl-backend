package com.mopl.external.tsdb.model;

public record EventItem(
    Long idEvent,
    String strEvent,
    String strFilename,
    String strSport,
    String strHomeTeam,
    String strAwayTeam,
    String strThumb
) {
}
