package com.program.file_conversion_service.service.outbox;

import com.program.file_conversion_service.config.properties.OutboxProperties;
import com.program.file_conversion_service.config.properties.KafkaTopicsProperties;
import com.program.file_conversion_service.domain.dao.OutboxEventRepository;
import com.program.file_conversion_service.domain.model.OutboxEventEntity;
import com.program.file_conversion_service.domain.model.OutboxEventType;
import com.program.file_conversion_service.domain.model.OutboxStatus;
import com.program.file_conversion_service.kafka.producer.ConversionEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxService outboxService;
    private final ConversionEventPublisher conversionEventPublisher;
    private final OutboxProperties outboxProperties;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @Scheduled(fixedDelayString = "${app.outbox.publish-interval-ms}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEventEntity> pendingEvents = outboxEventRepository.findNextBatchForUpdate(
                OutboxStatus.PENDING.name(),
                outboxProperties.batchSize()
        );

        for (OutboxEventEntity event : pendingEvents) {
            try {
                Object payload = outboxService.deserializePayload(event);
                conversionEventPublisher.publish(
                        topicName(event.getEventType()),
                        event.getPartitionKey(),
                        payload,
                        "outbox event " + event.getId()
                );
                outboxService.markSent(event);
            } catch (Exception exception) {
                log.error("Failed to publish outbox event {}", event.getId(), exception);
                outboxService.registerPublishFailure(
                        event,
                        exception.getMessage(),
                        outboxProperties.maxPublishAttempts(),
                        outboxProperties.publishIntervalMs()
                );
            }
        }
    }

    private String topicName(OutboxEventType eventType) {
        return switch (eventType) {
            case CONVERSION_REQUEST -> kafkaTopicsProperties.inputTopic();
            case CONVERSION_RESULT -> kafkaTopicsProperties.outputTopic();
        };
    }
}
