package com.program.file_conversion_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.outbox")
public record OutboxProperties(
        long publishIntervalMs,
        int batchSize,
        int maxPublishAttempts
) {
}
