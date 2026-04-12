package com.program.file_conversion_service.service.outbox;

import com.program.file_conversion_service.domain.dao.OutboxEventRepository;
import com.program.file_conversion_service.domain.model.OutboxEventEntity;
import com.program.file_conversion_service.domain.model.OutboxEventType;
import com.program.file_conversion_service.domain.model.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository repository;
    private final OutboxPayloadMapper payloadMapper;

    public void enqueue(
            String aggregateType,
            UUID aggregateId,
            OutboxEventType eventType,
            String dedupKey,
            String partitionKey,
            Object payload
    ) {
        if (repository.existsByDedupKey(dedupKey)) {
            return;
        }

        repository.save(OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .dedupKey(dedupKey)
                .partitionKey(partitionKey)
                .payload(payloadMapper.serialize(eventType, payload))
                .status(OutboxStatus.PENDING)
                .attempts(0)
                .availableAt(LocalDateTime.now())
                .build());
    }

    public void markSent(OutboxEventEntity event) {
        event.setStatus(OutboxStatus.SENT);
        event.setPublishedAt(LocalDateTime.now());
        event.setLastError(null);
    }

    public void registerPublishFailure(
            OutboxEventEntity event,
            String errorMessage,
            int maxPublishAttempts,
            long retryDelayMs
    ) {
        event.setAttempts(event.getAttempts() + 1);
        event.setLastError(errorMessage);
        if (event.getAttempts() >= maxPublishAttempts) {
            event.setStatus(OutboxStatus.FAILED);
        } else {
            event.setStatus(OutboxStatus.PENDING);
            event.setAvailableAt(LocalDateTime.now().plusNanos(retryDelayMs * 1_000_000));
        }
    }

    public Object deserializePayload(OutboxEventEntity event) {
        return payloadMapper.deserialize(event.getEventType(), event.getPayload());
    }
}
