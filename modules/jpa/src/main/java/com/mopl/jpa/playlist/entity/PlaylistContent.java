package com.mopl.jpa.playlist.entity;

import com.mopl.jpa.global.auditing.CreatedOnlyEntity;
import com.mopl.jpa.content.entity.Content;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_content")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaylistContent extends CreatedOnlyEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Content content;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}