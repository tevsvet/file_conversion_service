package com.program.file_conversion_service.service.request;

import com.program.file_conversion_service.config.properties.KafkaTopicsProperties;
import com.program.file_conversion_service.config.properties.MinioProperties;
import com.program.file_conversion_service.domain.model.ConversionTaskEntity;
import com.program.file_conversion_service.domain.model.OutboxEventType;
import com.program.file_conversion_service.kafka.dto.ConvertRequest;
import com.program.file_conversion_service.service.outbox.OutboxService;
import com.program.file_conversion_service.service.task.ConversionTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversionRequestService {

    private final ConversionTaskService conversionTaskService;
    private final OutboxService outboxService;
    private final KafkaTopicsProperties kafkaTopicsProperties;
    private final MinioProperties minioProperties;

    @Transactional
    public ConversionTaskEntity submit(ConvertRequest request) {
        ConversionTaskEntity task = conversionTaskService.registerPending(request, minioProperties.defaultBucket());
        if (!conversionTaskService.isSubmittable(task)) {
            return task;
        }
        outboxService.enqueue(
                "CONVERSION_TASK",
                request.taskId(),
                OutboxEventType.CONVERSION_REQUEST,
                "conversion-request:" + request.taskId(),
                request.taskId().toString(),
                request
        );
        return task;
    }
}
