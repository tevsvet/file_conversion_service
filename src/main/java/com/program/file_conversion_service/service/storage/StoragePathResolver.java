package com.program.file_conversion_service.service.storage;

import com.program.file_conversion_service.config.properties.ConversionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoragePathResolver {

    private final ConversionProperties conversionProperties;

    public String toPdfObjectKey(String sourceObjectKey) {
        int extensionIndex = sourceObjectKey.lastIndexOf('.');
        String baseName = extensionIndex > 0 ? sourceObjectKey.substring(0, extensionIndex) : sourceObjectKey;
        String normalizedPrefix = trimSlashes(conversionProperties.resultPrefix());
        return normalizedPrefix.isBlank() ? baseName + ".pdf" : normalizedPrefix + "/" + baseName + ".pdf";
    }

    private String trimSlashes(String value) {
        return value == null ? "" : value.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
