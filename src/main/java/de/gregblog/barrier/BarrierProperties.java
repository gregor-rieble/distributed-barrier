package de.gregblog.barrier;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "barrier")
public record BarrierProperties(
    int numParties,
    int defaultTimeoutSeconds,
    int shutdownAfterCompletions
) {
}
