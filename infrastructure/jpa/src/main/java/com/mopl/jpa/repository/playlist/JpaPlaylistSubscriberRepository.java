package com.mopl.jpa.repository.playlist;

import com.mopl.jpa.entity.playlist.PlaylistSubscriberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaPlaylistSubscriberRepository extends
    JpaRepository<PlaylistSubscriberEntity, UUID> {

    boolean existsByPlaylist_IdAndSubscriber_Id(UUID playlistId, UUID subscriberId);

    void deleteByPlaylist_IdAndSubscriber_Id(UUID playlistId, UUID subscriberId);

}
