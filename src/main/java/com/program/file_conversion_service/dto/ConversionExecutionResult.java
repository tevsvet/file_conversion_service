package com.program.file_conversion_service.dto;

public record ConversionExecutionResult(
        String resultBucket,
        String resultObjectKey
) { }
