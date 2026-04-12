package com.program.file_conversion_service.kafka.consumer;

import com.program.file_conversion_service.kafka.dto.ConvertRequest;
import com.program.file_conversion_service.service.conversion.FileConversionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileConversionConsumer {

    private final FileConversionService fileConversionService;

    @KafkaListener(topics = "${app.kafka.input-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(ConvertRequest request, Acknowledgment acknowledgment) {
        log.info("Received conversion request: taskId={}, sourceObjectKey={}", request.taskId(), request.sourceObjectKey());
        fileConversionService.process(request);
        acknowledgment.acknowledge();
    }
}
