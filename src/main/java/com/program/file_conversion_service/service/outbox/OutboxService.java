package com.program.file_conversion_service.service.outbox;

import com.program.file_conversion_service.domain.dao.OutboxEventRepository;
import com.program.file_conversion_service.domain.model.OutboxEventEntity;
import com.program.file_conversion_service.domain.model.OutboxStatus;
import com.program.file_conversion_service.kafka.dto.ConvertResult;
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
            String dedupKey,
            String partitionKey,
            ConvertResult payload
    ) {
        if (repository.existsByDedupKey(dedupKey)) {
            return;
        }

        repository.save(OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .dedupKey(dedupKey)
                .partitionKey(partitionKey)
                .payload(payloadMapper.serialize(payload))
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

    public ConvertResult deserializePayload(OutboxEventEntity event) {
        return payloadMapper.deserialize(event.getPayload());
    }
}
