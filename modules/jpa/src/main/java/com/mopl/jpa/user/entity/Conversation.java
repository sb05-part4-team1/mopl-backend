package com.mopl.jpa.user.entity;

import com.mopl.jpa.global.auditing.BaseUpdatableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conversation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Conversation extends BaseUpdatableEntity {
}
