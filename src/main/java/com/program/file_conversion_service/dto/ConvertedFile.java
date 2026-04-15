package com.program.file_conversion_service.dto;

public record ConvertedFile(
        byte[] content,
        String contentType
) {

    public long size() {
        return content.length;
    }
}
