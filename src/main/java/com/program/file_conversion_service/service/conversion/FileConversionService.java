package com.program.file_conversion_service.service.conversion;

import com.program.file_conversion_service.config.properties.KafkaTopicsProperties;
import com.program.file_conversion_service.config.properties.MinioProperties;
import com.program.file_conversion_service.domain.model.ConversionStatus;
import com.program.file_conversion_service.domain.model.OutboxEventType;
import com.program.file_conversion_service.kafka.dto.ConvertRequest;
import com.program.file_conversion_service.kafka.dto.ConvertResult;
import com.program.file_conversion_service.minio.MinioService;
import com.program.file_conversion_service.service.outbox.OutboxService;
import com.program.file_conversion_service.service.task.ConversionTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileConversionService {

    private final MinioService minioService;
    private final ConversionTaskService conversionTaskService;
    private final MinioProperties minioProperties;
    private final KafkaTopicsProperties kafkaTopicsProperties;
    private final OutboxService outboxService;
    private final ConversionExecutor conversionExecutor;

    @Transactional
    public void process(ConvertRequest request) {
        String sourceBucket = minioService.resolveBucket(request.bucket());
        conversionTaskService.registerPending(request, minioProperties.defaultBucket());

        if (!conversionTaskService.tryMarkInProgress(request.taskId())) {
            log.info("Skipping duplicate conversion processing for taskId={}", request.taskId());
            return;
        }

        try {
            ConversionExecutionResult executionResult = conversionExecutor.execute(sourceBucket, request);
            conversionTaskService.markSuccess(request.taskId(), executionResult.resultBucket(), executionResult.resultObjectKey());
            outboxService.enqueue(
                    "CONVERSION_TASK",
                    request.taskId(),
                    OutboxEventType.CONVERSION_RESULT,
                    "conversion-result:" + request.taskId(),
                    request.taskId().toString(),
                    new ConvertResult(
                            request.taskId(),
                            sourceBucket,
                            request.sourceObjectKey(),
                            executionResult.resultBucket(),
                            executionResult.resultObjectKey(),
                            ConversionStatus.SUCCESS,
                            null
                    )
            );
        } catch (Exception ex) {
            log.error("Conversion failed for taskId={}", request.taskId(), ex);
            conversionTaskService.markFailed(request.taskId(), ex.getMessage());
            outboxService.enqueue(
                    "CONVERSION_TASK",
                    request.taskId(),
                    OutboxEventType.CONVERSION_RESULT,
                    "conversion-result:" + request.taskId(),
                    request.taskId().toString(),
                    new ConvertResult(
                            request.taskId(),
                            sourceBucket,
                            request.sourceObjectKey(),
                            sourceBucket,
                            null,
                            ConversionStatus.FAILED,
                            ex.getMessage()
                    )
            );
        }
    }
}
