package com.mopl.batch.cleanup.retention.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RetentionCleanupPolicyProperties {

    private Integer chunkSize;
    private Long retentionDays;
}
