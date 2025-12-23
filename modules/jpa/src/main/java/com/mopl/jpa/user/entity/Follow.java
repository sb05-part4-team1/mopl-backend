package com.mopl.jpa.user.entity;

import com.mopl.jpa.global.auditing.CreatedOnlyEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "follow")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow extends CreatedOnlyEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followee_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User followee;
}