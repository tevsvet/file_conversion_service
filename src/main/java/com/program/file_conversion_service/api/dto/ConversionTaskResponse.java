package com.program.file_conversion_service.api.dto;

import com.program.file_conversion_service.domain.model.ConversionStatus;
import com.program.file_conversion_service.domain.model.SupportedFileType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversionTaskResponse(
        @Schema(description = "Unique conversion task identifier", example = "c24b3947-b35f-46a4-8e5d-becfae94ea77")
        UUID id,
        @Schema(description = "Current conversion task status", example = "SUCCESS")
        ConversionStatus status,
        @Schema(description = "Bucket containing the original uploaded or referenced file", example = "files")
        String sourceBucket,
        @Schema(description = "Object key of the original source file", example = "uploads/contracts/sample.txt")
        String sourceObjectKey,
        @Schema(description = "Resolved source file type", example = "TXT")
        SupportedFileType sourceFileType,
        @Schema(description = "Bucket containing the converted PDF result", example = "files")
        String resultBucket,
        @Schema(description = "Object key of the converted PDF file", example = "converted/contracts/sample.pdf")
        String resultObjectKey,
        @Schema(description = "Error message for failed tasks. Empty for successful or in-progress tasks.", example = "Unsupported file type: DOCX")
        String errorMessage,
        @Schema(description = "Task creation timestamp")
        LocalDateTime createdAt,
        @Schema(description = "Last task update timestamp")
        LocalDateTime updatedAt
) { }
