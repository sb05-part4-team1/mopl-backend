package com.mopl.batch.cleanup.orphan.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrphanCleanupPolicyProperties {

    private Integer chunkSize;
    private Long gracePeriodDays;
}
