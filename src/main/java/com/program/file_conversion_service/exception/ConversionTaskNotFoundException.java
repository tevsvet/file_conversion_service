package com.program.file_conversion_service.exception;

public class ConversionTaskNotFoundException extends RuntimeException {

    public ConversionTaskNotFoundException(String message) {
        super(message);
    }
}
