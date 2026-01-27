package com.mopl.jpa.entity.content;

import com.mopl.jpa.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "content_deletion_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class ContentDeletionLogEntity extends BaseEntity {

    @Column(name = "content_id", nullable = false, columnDefinition = "BINARY(16)", unique = true)
    private UUID contentId;

    @Column(name = "thumbnail_path", nullable = false, length = 1024)
    private String thumbnailPath;

    @Column(name = "image_processed_at")
    private Instant imageProcessedAt;
}
