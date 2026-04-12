package com.program.file_conversion_service.domain.model;

import com.program.file_conversion_service.exception.UnsupportedFileTypeException;
import org.springframework.util.StringUtils;

import java.util.Locale;

public enum SupportedFileType {
    TXT,
    PNG,
    JPG,
    JPEG,
    ZIP;

    public static SupportedFileType resolve(String rawType, String objectKey) {
        if (StringUtils.hasText(rawType)) {
            return from(rawType);
        }
        return fromExtension(objectKey);
    }

    public static SupportedFileType fromExtension(String objectKey) {
        int extensionIndex = objectKey.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == objectKey.length() - 1) {
            throw new UnsupportedFileTypeException("File type cannot be inferred from object key: " + objectKey);
        }
        return from(objectKey.substring(extensionIndex + 1));
    }

    public static SupportedFileType from(String value) {
        try {
            return SupportedFileType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new UnsupportedFileTypeException("Unsupported file type: " + value, ex);
        }
    }

    public SupportedFileType normalized() {
        return this == JPEG ? JPG : this;
    }
}
