package com.mopl.jpa.user.entity;

import com.mopl.jpa.global.auditing.BaseIdEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "read_status")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadStatus extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User participant;

    @Column(nullable = false)
    private LocalDateTime lastReadAt;
}