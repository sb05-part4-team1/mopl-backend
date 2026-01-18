package com.mopl.external.tsdb.model;

import java.util.List;

public record EventResponse(
    List<EventItem> events
) {
}
