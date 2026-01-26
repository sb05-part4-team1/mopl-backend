package com.mopl.batch.cleanup.config.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CleanupPolicyProperties {

    private Integer chunkSize;
    private Long retentionDays;
}
