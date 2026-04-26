package com.program.file_conversion_service.config.kafka;

import com.program.file_conversion_service.config.properties.KafkaTopicsProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@RequiredArgsConstructor
public class KafkaListenerErrorHandlingConfig {

    private final KafkaTopicsProperties kafkaTopicsProperties;

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaOperations<Object, Object> kafkaOperations) {
        return new DeadLetterPublishingRecoverer(
                kafkaOperations,
                (record, exception) -> new TopicPartition(
                        record.topic() + kafkaTopicsProperties.deadLetterSuffix(),
                        record.partition()
                )
        );
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        long retries = Math.max(0, kafkaTopicsProperties.consumerMaxAttempts() - 1);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(kafkaTopicsProperties.consumerBackoffMs(), retries)
        );
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }
}
