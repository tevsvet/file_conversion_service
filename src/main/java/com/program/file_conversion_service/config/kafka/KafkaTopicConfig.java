package com.program.file_conversion_service.config.kafka;

import com.program.file_conversion_service.config.properties.KafkaTopicsProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final KafkaTopicsProperties kafkaTopicsProperties;

    @Bean
    public KafkaAdmin.NewTopics conversionTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(kafkaTopicsProperties.inputTopic())
                        .partitions(kafkaTopicsProperties.partitions())
                        .replicas(kafkaTopicsProperties.replicationFactor())
                        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, String.valueOf(kafkaTopicsProperties.minInSyncReplicas()))
                        .build(),
                TopicBuilder.name(kafkaTopicsProperties.outputTopic())
                        .partitions(kafkaTopicsProperties.partitions())
                        .replicas(kafkaTopicsProperties.replicationFactor())
                        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, String.valueOf(kafkaTopicsProperties.minInSyncReplicas()))
                        .build(),
                TopicBuilder.name(kafkaTopicsProperties.inputDeadLetterTopic())
                        .partitions(kafkaTopicsProperties.partitions())
                        .replicas(kafkaTopicsProperties.replicationFactor())
                        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, String.valueOf(kafkaTopicsProperties.minInSyncReplicas()))
                        .build()
        );
    }
}
