package com.program.file_conversion_service.api.dto;

public record ConvertedFile(
        byte[] content,
        String contentType
) {

    public long size() {
        return content.length;
    }
}
