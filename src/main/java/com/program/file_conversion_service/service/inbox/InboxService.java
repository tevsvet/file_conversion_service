package com.program.file_conversion_service.service.inbox;

import com.program.file_conversion_service.config.properties.InboxProperties;
import com.program.file_conversion_service.domain.dao.InboxEventRepository;
import com.program.file_conversion_service.domain.model.InboxEventEntity;
import com.program.file_conversion_service.domain.model.InboxStatus;
import com.program.file_conversion_service.exception.DuplicateInboxEventException;
import com.program.file_conversion_service.kafka.dto.ConvertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InboxService {

    private final InboxEventRepository inboxEventRepository;
    private final InboxPayloadMapper inboxPayloadMapper;
    private final InboxProperties inboxProperties;

    @Transactional
    public void registerReceived(ConvertRequest request, String sourceBucket) {
        saveReceived(request, sourceBucket);
    }

    @Transactional
    public List<InboxEventEntity> claimNextBatch() {
        List<InboxEventEntity> batch = inboxEventRepository.findNextBatchForUpdate(
                InboxStatus.RECEIVED.name(),
                inboxProperties.batchSize()
        );
        for (InboxEventEntity inboxEvent : batch) {
            inboxEvent.setStatus(InboxStatus.PROCESSING);
            inboxEvent.setLastError(null);
        }
        return batch;
    }

    @Transactional
    public void recoverStaleProcessingEvents() {
        LocalDateTime threshold = LocalDateTime.now()
                .minus(Duration.ofMillis(inboxProperties.processingTimeoutMs()));
        List<InboxEventEntity> batch = inboxEventRepository.findTimedOutProcessingBatchForUpdate(
                InboxStatus.PROCESSING.name(),
                threshold,
                inboxProperties.batchSize()
        );
        for (InboxEventEntity inboxEvent : batch) {
            int attempts = inboxEvent.getAttempts() + 1;
            inboxEvent.setAttempts(attempts);
            inboxEvent.setLastError("Processing lease expired");
            if (attempts >= inboxProperties.maxProcessingAttempts()) {
                inboxEvent.setStatus(InboxStatus.FAILED);
            } else {
                inboxEvent.setStatus(InboxStatus.RECEIVED);
                inboxEvent.setAvailableAt(LocalDateTime.now().plusNanos(inboxProperties.retryDelayMs() * 1_000_000));
            }
        }
    }

    @Transactional
    public void markProcessed(UUID inboxEventId) {
        InboxEventEntity inboxEvent = getById(inboxEventId);
        inboxEvent.setStatus(InboxStatus.PROCESSED);
        inboxEvent.setProcessedAt(LocalDateTime.now());
        inboxEvent.setLastError(null);
    }

    @Transactional
    public void registerFailure(UUID inboxEventId, String errorMessage) {
        InboxEventEntity inboxEvent = getById(inboxEventId);
        int attempts = inboxEvent.getAttempts() + 1;
        inboxEvent.setAttempts(attempts);
        inboxEvent.setLastError(errorMessage);

        if (attempts >= inboxProperties.maxProcessingAttempts()) {
            inboxEvent.setStatus(InboxStatus.FAILED);
        } else {
            inboxEvent.setStatus(InboxStatus.RECEIVED);
            inboxEvent.setAvailableAt(LocalDateTime.now().plusNanos(inboxProperties.retryDelayMs() * 1_000_000));
        }
    }

    public ConvertRequest deserializePayload(InboxEventEntity inboxEvent) {
        return inboxPayloadMapper.deserialize(inboxEvent.getPayload());
    }

    private InboxEventEntity getById(UUID inboxEventId) {
        return inboxEventRepository.findById(inboxEventId)
                .orElseThrow(() -> new NoSuchElementException("Inbox event not found: " + inboxEventId));
    }

    private InboxEventEntity saveReceived(ConvertRequest request, String sourceBucket) {
        try {
            return inboxEventRepository.saveAndFlush(
                    InboxEventEntity.builder()
                            .id(UUID.randomUUID())
                            .taskId(request.taskId())
                            .status(InboxStatus.RECEIVED)
                            .sourceBucket(sourceBucket)
                            .sourceObjectKey(request.sourceObjectKey())
                            .payload(inboxPayloadMapper.serialize(request))
                            .attempts(0)
                            .build()
            );
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateInboxEventException(request.taskId(), exception);
        }
    }
}
