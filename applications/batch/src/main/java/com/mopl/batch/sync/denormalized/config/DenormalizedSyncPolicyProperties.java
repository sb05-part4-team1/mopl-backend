package com.mopl.batch.sync.denormalized.config;

public record DenormalizedSyncPolicyProperties(
    int batchSize
) {

    public static DenormalizedSyncPolicyProperties defaults() {
        return new DenormalizedSyncPolicyProperties(500);
    }
}
