package com.mopl.batch.cleanup.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.cleanup")
@Getter
@Setter
public class CleanupProperties {

    private CleanupPolicyProperties defaults = new CleanupPolicyProperties();

    private CleanupPolicyProperties content = new CleanupPolicyProperties();
    private CleanupPolicyProperties storage = new CleanupPolicyProperties();
    private CleanupPolicyProperties deletionLog = new CleanupPolicyProperties();

    @PostConstruct
    public void validate() {
        Integer chunkSize = defaults.getChunkSize();
        Long retentionDays = defaults.getRetentionDays();

        if (chunkSize == null || chunkSize <= 0) {
            throw new IllegalStateException("mopl.batch.cleanup.defaults.chunk-size is required");
        }
        if (retentionDays == null || retentionDays < 0) {
            throw new IllegalStateException(
                "mopl.batch.cleanup.defaults.retention-days is required");
        }
    }
}
