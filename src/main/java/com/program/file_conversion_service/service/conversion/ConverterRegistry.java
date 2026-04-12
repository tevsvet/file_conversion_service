package com.program.file_conversion_service.service.conversion;

import com.program.file_conversion_service.domain.model.SupportedFileType;
import com.program.file_conversion_service.exception.UnsupportedFileTypeException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ConverterRegistry {

    private final Map<SupportedFileType, FileToPdfConverter> converters = new EnumMap<>(SupportedFileType.class);

    public ConverterRegistry(List<FileToPdfConverter> registeredConverters) {
        for (FileToPdfConverter converter : registeredConverters) {
            for (SupportedFileType supportedType : converter.supportedTypes()) {
                converters.put(supportedType.normalized(), converter);
            }
        }
    }

    public FileToPdfConverter getConverter(SupportedFileType fileType) {
        FileToPdfConverter converter = converters.get(fileType.normalized());
        if (converter == null) {
            throw new UnsupportedFileTypeException("No converter registered for type: " + fileType);
        }
        return converter;
    }
}
