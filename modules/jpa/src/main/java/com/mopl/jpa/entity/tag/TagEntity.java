package com.mopl.jpa.entity.tag;

import com.mopl.jpa.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tags")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
