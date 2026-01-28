package com.mopl.external;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class ExternalApiMetrics {

    private final MeterRegistry registry;

    public ExternalApiMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }

    public void recordSuccess(Timer.Sample sample, String api, String endpoint) {
        sample.stop(Timer.builder("mopl.external.api.latency")
            .tag("api", api)
            .tag("endpoint", endpoint)
            .tag("status", "success")
            .register(registry));

        Counter.builder("mopl.external.api.request")
            .tag("api", api)
            .tag("endpoint", endpoint)
            .tag("status", "success")
            .register(registry)
            .increment();
    }

    public void recordError(Timer.Sample sample, String api, String endpoint, String errorType) {
        sample.stop(Timer.builder("mopl.external.api.latency")
            .tag("api", api)
            .tag("endpoint", endpoint)
            .tag("status", "error")
            .register(registry));

        Counter.builder("mopl.external.api.request")
            .tag("api", api)
            .tag("endpoint", endpoint)
            .tag("status", "error")
            .register(registry)
            .increment();

        Counter.builder("mopl.external.api.error")
            .tag("api", api)
            .tag("endpoint", endpoint)
            .tag("error_type", errorType)
            .register(registry)
            .increment();
    }

    public void recordImageDownload(String api, long bytes) {
        Counter.builder("mopl.external.api.image.download")
            .tag("api", api)
            .register(registry)
            .increment();

        Counter.builder("mopl.external.api.image.bytes")
            .tag("api", api)
            .register(registry)
            .increment(bytes);
    }
}
