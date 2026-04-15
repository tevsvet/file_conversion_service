package com.program.file_conversion_service.kafka.producer;

import com.program.file_conversion_service.config.properties.KafkaTopicsProperties;
import com.program.file_conversion_service.kafka.dto.ConvertResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, String key, Object payload, String payloadDescription) {
        try {
            SendResult<String, Object> sendResult = kafkaTemplate.send(topic, key, payload).join();
            RecordMetadata metadata = sendResult.getRecordMetadata();
            log.info(
                    "Published {} to topic={}, partition={}, offset={}, key={}",
                    payloadDescription,
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    key
            );
        } catch (CompletionException ex) {
            throw new RuntimeException("Kafka publish failed for " + payloadDescription + " with key=" + key, ex.getCause());
        }
    }
}
