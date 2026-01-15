package com.mopl.domain.repository.playlist;

import java.util.UUID;

public interface PlaylistSubscriberCountRepository {

    long getCount(UUID playlistId);

    void increment(UUID playlistId);

    void decrement(UUID playlistId);

    void setCount(UUID playlistId, long count);
}
