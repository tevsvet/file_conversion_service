package com.program.file_conversion_service.service.conversion;

import com.program.file_conversion_service.domain.model.ConversionStatus;
import com.program.file_conversion_service.dto.ConversionExecutionResult;
import com.program.file_conversion_service.kafka.dto.ConvertRequest;
import com.program.file_conversion_service.kafka.dto.ConvertResult;
import com.program.file_conversion_service.minio.MinioService;
import com.program.file_conversion_service.service.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileConversionService {

    private final MinioService minioService;
    private final OutboxService outboxService;
    private final ConversionExecutor conversionExecutor;

    @Transactional
    public void process(ConvertRequest request) {
        String sourceBucket = minioService.resolveBucket(request.bucket());

        try {
            ConversionExecutionResult executionResult = conversionExecutor.execute(sourceBucket, request);
            outboxService.enqueue(
                    "FILE_CONVERSION",
                    request.taskId(),
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
            outboxService.enqueue(
                    "FILE_CONVERSION",
                    request.taskId(),
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
