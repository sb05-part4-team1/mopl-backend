package com.mopl.jpa.playlist.entity;

import com.mopl.jpa.global.auditing.CreatedOnlyEntity;
import com.mopl.jpa.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_subscriber")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaylistSubscriber extends CreatedOnlyEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User subscriber;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}