package com.mopl.jpa.entity.review;

import com.mopl.jpa.entity.base.BaseUpdatableEntity;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
    name = "reviews",
    indexes = {
        @Index(name = "idx_reviews_content_created_at", columnList = "content_id, created_at DESC"),
        @Index(name = "idx_reviews_content_rating", columnList = "content_id, rating DESC"),
        @Index(name = "idx_reviews_deleted_at", columnList = "deleted_at"),
        @Index(name = "idx_reviews_author_id", columnList = "author_id"),
        @Index(name = "idx_reviews_created_at", columnList = "created_at")
    }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class ReviewEntity extends BaseUpdatableEntity {

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false)
    private double rating;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ContentEntity content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UserEntity author;
}
