package com.program.file_conversion_service.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConvertRequest(
        UUID taskId,
        String bucket,
        String sourceObjectKey,
        String fileType
) { }
