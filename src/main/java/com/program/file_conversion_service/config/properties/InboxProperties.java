package com.program.file_conversion_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.inbox")
public record InboxProperties(
        long pollIntervalMs,
        int batchSize,
        int maxProcessingAttempts,
        long retryDelayMs,
        long processingTimeoutMs
) { }
