package com.mopl.jpa.entity.content;

import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.jpa.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import static com.mopl.domain.model.content.ContentModel.CONTENT_TYPE_MAX_LENGTH;
import static com.mopl.domain.model.content.ContentModel.THUMBNAIL_PATH_MAX_LENGTH;

@Entity
@Table(
    name = "contents",
    indexes = {
        @Index(name = "idx_contents_deleted_at", columnList = "deleted_at")
    }
)
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class ContentEntity extends BaseUpdatableEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = CONTENT_TYPE_MAX_LENGTH)
    private ContentType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = THUMBNAIL_PATH_MAX_LENGTH)
    private String thumbnailPath;

    @Column(nullable = false)
    private int reviewCount;

    @Column(nullable = false)
    private double averageRating;

    @Column(nullable = false)
    private double popularityScore;
}
