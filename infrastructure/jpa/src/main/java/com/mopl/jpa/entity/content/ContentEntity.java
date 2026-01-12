package com.mopl.jpa.entity.content;

import com.mopl.domain.model.content.ContentModel.ContentType; // 내부 이넘 임포트
import com.mopl.jpa.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import static com.mopl.domain.model.content.ContentModel.THUMBNAIL_URL_MAX_LENGTH;
import static com.mopl.domain.model.content.ContentModel.TITLE_MAX_LENGTH;

@Entity
@Table(name = "contents")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class ContentEntity extends BaseUpdatableEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentType type;

    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = THUMBNAIL_URL_MAX_LENGTH)
    private String thumbnailUrl;

    @Column(nullable = false)
    private double averageRating;

    @Column(nullable = false)
    private int reviewCount;
}
