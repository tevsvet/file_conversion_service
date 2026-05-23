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
    private final KafkaTopicsProperties kafkaTopicsProperties;

    public void publish(ConvertResult payload) {
        String key = payload.taskId().toString();
        String topic = kafkaTopicsProperties.outputTopic();
        try {
            SendResult<String, Object> sendResult = kafkaTemplate.send(topic, key, payload).join();
            RecordMetadata metadata = sendResult.getRecordMetadata();
            log.info(
                    "Published conversion result to topic={}, partition={}, offset={}, key={}",
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    key
            );
        } catch (CompletionException ex) {
            throw new RuntimeException("Kafka publish failed for conversion result with key=" + key, ex.getCause());
        }
    }
}
