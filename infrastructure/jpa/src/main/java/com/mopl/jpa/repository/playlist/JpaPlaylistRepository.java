package com.mopl.jpa.repository.playlist;

import com.mopl.jpa.entity.playlist.PlaylistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaPlaylistRepository extends JpaRepository<PlaylistEntity, UUID> {
}
