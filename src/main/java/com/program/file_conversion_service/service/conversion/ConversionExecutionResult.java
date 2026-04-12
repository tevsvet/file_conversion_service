package com.program.file_conversion_service.service.conversion;

public record ConversionExecutionResult(
        String resultBucket,
        String resultObjectKey
) {
}
