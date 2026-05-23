package com.program.file_conversion_service.kafka.consumer;

import com.program.file_conversion_service.exception.DuplicateInboxEventException;
import com.program.file_conversion_service.kafka.dto.ConvertRequest;
import com.program.file_conversion_service.minio.MinioService;
import com.program.file_conversion_service.service.inbox.InboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileConversionConsumer {

    private final InboxService inboxService;
    private final MinioService minioService;

    @KafkaListener(topics = "${app.kafka.input-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(ConvertRequest request, Acknowledgment acknowledgment) {
        log.info("Received conversion request: taskId={}, sourceObjectKey={}", request.taskId(), request.sourceObjectKey());
        validateRequest(request);
        String sourceBucket = minioService.resolveBucket(request.bucket());
        try {
            inboxService.registerReceived(request, sourceBucket);
        } catch (DuplicateInboxEventException exception) {
            log.info("Skipping duplicate conversion request for taskId={}", request.taskId());
            acknowledgment.acknowledge();
            return;
        }
        acknowledgment.acknowledge();
    }

    private void validateRequest(ConvertRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }
        if (request.taskId() == null) {
            throw new IllegalArgumentException("taskId must not be null");
        }
        if (request.sourceObjectKey() == null || request.sourceObjectKey().isBlank()) {
            throw new IllegalArgumentException("sourceObjectKey must not be blank");
        }
        if (request.bucket() == null || request.bucket().isBlank()) {
            throw new IllegalArgumentException("bucket must not be blank");
        }
    }
}
