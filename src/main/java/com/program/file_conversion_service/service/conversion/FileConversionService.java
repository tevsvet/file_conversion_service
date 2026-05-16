package com.program.file_conversion_service.service.conversion;

import com.program.file_conversion_service.domain.model.ConversionStatus;
import com.program.file_conversion_service.dto.ConversionExecutionResult;
import com.program.file_conversion_service.domain.model.InboxEventEntity;
import com.program.file_conversion_service.kafka.dto.ConvertRequest;
import com.program.file_conversion_service.kafka.dto.ConvertResult;
import com.program.file_conversion_service.kafka.producer.ConversionEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileConversionService {

    private final ConversionExecutor conversionExecutor;
    private final ConversionEventPublisher conversionEventPublisher;

    public ConvertResult process(InboxEventEntity inboxEvent, ConvertRequest request) {
        String sourceBucket = inboxEvent.getSourceBucket();

        try {
            ConversionExecutionResult executionResult = conversionExecutor.execute(sourceBucket, request);
            ConvertResult result = new ConvertResult(
                    request.taskId(),
                    sourceBucket,
                    request.sourceObjectKey(),
                    executionResult.resultBucket(),
                    executionResult.resultObjectKey(),
                    ConversionStatus.SUCCESS,
                    null
            );
            conversionEventPublisher.publish(result);
            return result;
        } catch (Exception ex) {
            log.error("Conversion failed for taskId={}", request.taskId(), ex);
            ConvertResult result = new ConvertResult(
                    request.taskId(),
                    sourceBucket,
                    request.sourceObjectKey(),
                    sourceBucket,
                    null,
                    ConversionStatus.FAILED,
                    ex.getMessage()
            );
            conversionEventPublisher.publish(result);
            return result;
        }
    }
}
