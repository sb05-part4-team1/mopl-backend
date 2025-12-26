package com.mopl.jpa.entity.review;

import com.mopl.jpa.entity.base.BaseUpdatableEntity;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.user.UserEntity;
import jakarta.persistence.Column;
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

import java.math.BigDecimal;

@Entity
@Table(name = "reviews")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewEntity extends BaseUpdatableEntity {

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(precision = 2, scale = 1, nullable = false)
    private BigDecimal rating;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ContentEntity content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UserEntity author;
}
