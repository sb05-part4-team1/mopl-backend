package com.mopl.batch.cleanup.retention.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.cleanup.retention")
@Getter
@Setter
public class RetentionCleanupProperties {

    private RetentionCleanupPolicyProperties defaults = new RetentionCleanupPolicyProperties();

    private RetentionCleanupPolicyProperties content = new RetentionCleanupPolicyProperties();
    private RetentionCleanupPolicyProperties storage = new RetentionCleanupPolicyProperties();
    private RetentionCleanupPolicyProperties deletionLog = new RetentionCleanupPolicyProperties();

    @PostConstruct
    public void validate() {
        Integer chunkSize = defaults.getChunkSize();
        Long retentionDays = defaults.getRetentionDays();

        if (chunkSize == null || chunkSize <= 0) {
            throw new IllegalStateException("mopl.batch.cleanup.retention.defaults.chunk-size is required");
        }
        if (retentionDays == null || retentionDays < 0) {
            throw new IllegalStateException(
                "mopl.batch.cleanup.retention.defaults.retention-days is required");
        }
    }
}
