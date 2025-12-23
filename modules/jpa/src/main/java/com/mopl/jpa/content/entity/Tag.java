package com.mopl.jpa.content.entity;

import com.mopl.jpa.global.auditing.BaseDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseDeletableEntity {

    @Column(nullable = false, length = 50, unique = true)
    private String name;
}
