package com.mopl.domain.model.base;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@SuperBuilder
public abstract class BaseUpdatableModel extends BaseModel {

    private Instant updatedAt;

    protected BaseUpdatableModel() {
        super();
    }
}
