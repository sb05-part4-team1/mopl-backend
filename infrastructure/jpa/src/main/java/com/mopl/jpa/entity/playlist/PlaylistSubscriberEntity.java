package com.mopl.jpa.entity.playlist;

import com.mopl.jpa.entity.base.BaseEntity;
import com.mopl.jpa.entity.user.UserEntity;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "playlist_subscribers",
    indexes = {
        @Index(name = "idx_playlist_subscribers_subscriber_id", columnList = "subscriber_id")
    },
    uniqueConstraints = @UniqueConstraint(columnNames = {"playlist_id", "subscriber_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class PlaylistSubscriberEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PlaylistEntity playlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscriber_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UserEntity subscriber;
}
