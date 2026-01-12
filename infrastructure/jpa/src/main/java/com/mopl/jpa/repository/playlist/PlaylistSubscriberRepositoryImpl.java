package com.mopl.jpa.repository.playlist;

import com.mopl.domain.repository.playlist.PlaylistSubscriberRepository;
import com.mopl.jpa.entity.playlist.PlaylistEntity;
import com.mopl.jpa.entity.playlist.PlaylistSubscriberEntity;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.repository.user.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PlaylistSubscriberRepositoryImpl implements PlaylistSubscriberRepository {

    private final JpaPlaylistSubscriberRepository jpaPlaylistSubscriberRepository;
    private final JpaPlaylistRepository jpaPlaylistRepository;
    private final JpaUserRepository jpaUserRepository;

    @Override
    public boolean existsByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId) {
        return jpaPlaylistSubscriberRepository.existsByPlaylist_IdAndSubscriber_Id(
            playlistId,
            subscriberId
        );
    }

    @Override
    public void save(UUID playlistId, UUID subscriberId) {

        // FK 제약은 없지만, 참조 엔티티를 프록시로 잡아서 저장하는 방식 <- DB로 SELECT 쿼리 굳이 안나감
        PlaylistEntity playlistRef = jpaPlaylistRepository.getReferenceById(playlistId);
        UserEntity subscriberRef = jpaUserRepository.getReferenceById(subscriberId);

        PlaylistSubscriberEntity entity = PlaylistSubscriberEntity.builder()
            .playlist(playlistRef)
            .subscriber(subscriberRef)
            .build();

        jpaPlaylistSubscriberRepository.save(entity);
    }

    @Override
    public void deleteByPlaylistIdAndSubscriberId(UUID playlistId, UUID subscriberId) {
        jpaPlaylistSubscriberRepository.deleteByPlaylist_IdAndSubscriber_Id(
            playlistId,
            subscriberId
        );
    }

}
