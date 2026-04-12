package com.program.file_conversion_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaTopicsProperties(
        String inputTopic,
        String outputTopic,
        String deadLetterSuffix,
        int partitions,
        short replicationFactor,
        int minInSyncReplicas,
        long consumerMaxAttempts,
        long consumerBackoffMs
) {

    public String inputDeadLetterTopic() {
        return inputTopic + deadLetterSuffix;
    }
}
