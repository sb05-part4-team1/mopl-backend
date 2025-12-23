package com.mopl.jpa.user.entity;

import com.mopl.jpa.global.auditing.CreatedOnlyEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "direct_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectMessage extends CreatedOnlyEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;
}