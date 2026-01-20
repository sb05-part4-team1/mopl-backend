package com.mopl.domain.repository.playlist;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface PlaylistSubscriberCountRepository {

    long getCount(UUID playlistId);

    Map<UUID, Long> getCounts(Collection<UUID> playlistIds);

    void increment(UUID playlistId);

    void decrement(UUID playlistId);

    void setCount(UUID playlistId, long count);
}
