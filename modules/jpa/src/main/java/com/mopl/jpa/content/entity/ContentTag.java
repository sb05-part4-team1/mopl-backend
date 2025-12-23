package com.mopl.jpa.content.entity;

import com.mopl.jpa.global.auditing.CreatedOnlyEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentTag extends CreatedOnlyEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Tag tag;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}