package com.program.file_conversion_service.service.conversion;

import com.program.file_conversion_service.api.dto.ConvertedFile;
import com.program.file_conversion_service.domain.model.SupportedFileType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public interface FileToPdfConverter {

    Set<SupportedFileType> supportedTypes();

    ConvertedFile convert(InputStream inputStream, String sourceObjectKey) throws IOException;
}
