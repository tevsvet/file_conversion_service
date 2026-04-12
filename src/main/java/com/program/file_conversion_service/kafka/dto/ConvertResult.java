package com.program.file_conversion_service.kafka.dto;

import com.program.file_conversion_service.domain.model.ConversionStatus;

import java.util.UUID;

public record ConvertResult(
        UUID taskId,
        String sourceBucket,
        String sourceObjectKey,
        String resultBucket,
        String resultObjectKey,
        ConversionStatus status,
        String errorMessage
) { }
