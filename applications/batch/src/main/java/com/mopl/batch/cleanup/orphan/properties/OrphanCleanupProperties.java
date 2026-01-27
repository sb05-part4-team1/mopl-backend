package com.mopl.batch.cleanup.orphan.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mopl.batch.cleanup.orphan")
@Getter
@Setter
public class OrphanCleanupProperties {

    private OrphanCleanupPolicyProperties defaults = new OrphanCleanupPolicyProperties();

    private OrphanCleanupPolicyProperties notification = new OrphanCleanupPolicyProperties();
    private OrphanCleanupPolicyProperties follow = new OrphanCleanupPolicyProperties();
    private OrphanCleanupPolicyProperties playlistSubscriber = new OrphanCleanupPolicyProperties();
    private OrphanCleanupPolicyProperties playlistContent = new OrphanCleanupPolicyProperties();
    private OrphanCleanupPolicyProperties playlist = new OrphanCleanupPolicyProperties();
    private OrphanCleanupPolicyProperties review = new OrphanCleanupPolicyProperties();
    private OrphanCleanupPolicyProperties readStatus = new OrphanCleanupPolicyProperties();
    private OrphanCleanupPolicyProperties directMessage = new OrphanCleanupPolicyProperties();
    private OrphanCleanupPolicyProperties conversation = new OrphanCleanupPolicyProperties();
    private OrphanCleanupPolicyProperties contentTag = new OrphanCleanupPolicyProperties();
    private OrphanCleanupPolicyProperties contentExternalMapping = new OrphanCleanupPolicyProperties();

    @PostConstruct
    public void validate() {
        Integer chunkSize = defaults.getChunkSize();
        Long gracePeriodDays = defaults.getGracePeriodDays();

        if (chunkSize == null || chunkSize <= 0) {
            throw new IllegalStateException(
                "mopl.batch.cleanup.orphan.defaults.chunk-size is required");
        }
        if (gracePeriodDays == null || gracePeriodDays < 0) {
            throw new IllegalStateException(
                "mopl.batch.cleanup.orphan.defaults.grace-period-days is required");
        }
    }
}
