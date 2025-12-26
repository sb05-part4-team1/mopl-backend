package com.mopl.jpa.entity.playlist;

import com.mopl.jpa.entity.base.BaseEntity;
import com.mopl.jpa.entity.content.ContentEntity;
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
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "playlist_contents")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaylistContentEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PlaylistEntity playlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ContentEntity content;
}
