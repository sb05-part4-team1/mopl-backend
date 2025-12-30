package com.mopl.jpa.entity.content;

import com.mopl.jpa.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static com.mopl.domain.model.content.ContentModel.THUMBNAIL_URL_MAX_LENGTH;
import static com.mopl.domain.model.content.ContentModel.TITLE_MAX_LENGTH;
import static com.mopl.domain.model.content.ContentModel.TYPE_MAX_LENGTH;

@Entity
@Table(name = "contents")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentEntity extends BaseUpdatableEntity {

    @Column(nullable = false, length = TYPE_MAX_LENGTH)
    private String type;

    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = THUMBNAIL_URL_MAX_LENGTH)
    private String thumbnailUrl;
}
