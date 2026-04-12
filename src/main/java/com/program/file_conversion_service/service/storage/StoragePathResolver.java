package com.program.file_conversion_service.service.storage;

import com.program.file_conversion_service.config.properties.ConversionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StoragePathResolver {

    private final ConversionProperties conversionProperties;

    public String toSourceObjectKey(String originalFilename, UUID taskId) {
        String safeFilename = sanitizeFilename(originalFilename);
        String normalizedPrefix = trimSlashes(conversionProperties.sourcePrefix());
        String objectName = taskId + "-" + safeFilename;
        return normalizedPrefix.isBlank() ? objectName : normalizedPrefix + "/" + objectName;
    }

    public String toPdfObjectKey(String sourceObjectKey) {
        int extensionIndex = sourceObjectKey.lastIndexOf('.');
        String baseName = extensionIndex > 0 ? sourceObjectKey.substring(0, extensionIndex) : sourceObjectKey;
        String normalizedPrefix = trimSlashes(conversionProperties.resultPrefix());
        return normalizedPrefix.isBlank() ? baseName + ".pdf" : normalizedPrefix + "/" + baseName + ".pdf";
    }

    private String sanitizeFilename(String originalFilename) {
        String candidate = originalFilename == null || originalFilename.isBlank() ? "uploaded-file" : originalFilename;
        String normalized = candidate.replace('\\', '/');
        int separatorIndex = normalized.lastIndexOf('/');
        String filename = separatorIndex >= 0 ? normalized.substring(separatorIndex + 1) : normalized;
        filename = filename.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "-");
        filename = filename.replaceAll("-{2,}", "-");
        return filename.isBlank() ? "uploaded-file" : filename;
    }

    private String trimSlashes(String value) {
        return value == null ? "" : value.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
