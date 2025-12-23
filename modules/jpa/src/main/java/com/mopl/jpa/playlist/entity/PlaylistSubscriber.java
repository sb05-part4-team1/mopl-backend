package com.mopl.jpa.playlist.entity;

import com.mopl.jpa.global.auditing.BaseDeletableEntity;
import com.mopl.jpa.user.entity.User;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "playlist_subscriber")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaylistSubscriber extends BaseDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User subscriber;
}
