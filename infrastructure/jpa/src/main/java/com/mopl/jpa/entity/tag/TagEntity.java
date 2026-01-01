package com.mopl.jpa.entity.tag;

import com.mopl.jpa.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static com.mopl.domain.model.tag.TagModel.NAME_MAX_LENGTH;

@Entity
@Table(name = "tags")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = NAME_MAX_LENGTH)
    private String name;
}
