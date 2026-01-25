package com.mopl.batch.sync.denormalized.properties;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(DenormalizedSyncProperties.class)
public class DenormalizedSyncPolicyResolver {

    private final DenormalizedSyncProperties properties;
    private final Environment environment;

    public DenormalizedSyncPolicyProperties resolve() {
        if (properties == null) {
            return DenormalizedSyncPolicyProperties.defaults();
        }

        String[] activeProfiles = environment.getActiveProfiles();

        if (properties.profiles() != null) {
            for (String profile : activeProfiles) {
                DenormalizedSyncPolicyProperties profilePolicy = properties.profiles().get(profile);
                if (profilePolicy != null) {
                    return profilePolicy;
                }
            }
        }

        return properties.defaultPolicy() != null
            ? properties.defaultPolicy()
            : DenormalizedSyncPolicyProperties.defaults();
    }
}
