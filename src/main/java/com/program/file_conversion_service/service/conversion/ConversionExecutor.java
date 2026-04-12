package com.program.file_conversion_service.service.conversion;

import com.program.file_conversion_service.api.dto.ConvertedFile;
import com.program.file_conversion_service.config.properties.ConversionProperties;
import com.program.file_conversion_service.domain.model.SupportedFileType;
import com.program.file_conversion_service.exception.FileStorageException;
import com.program.file_conversion_service.kafka.dto.ConvertRequest;
import com.program.file_conversion_service.minio.MinioService;
import com.program.file_conversion_service.service.storage.StoragePathResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class ConversionExecutor {

    private final MinioService minioService;
    private final ConverterRegistry converterRegistry;
    private final StoragePathResolver storagePathResolver;
    private final ConversionProperties conversionProperties;

    public ConversionExecutionResult execute(String sourceBucket, ConvertRequest request) {
        try (InputStream inputStream = minioService.download(sourceBucket, request.sourceObjectKey())) {
            SupportedFileType fileType = SupportedFileType.resolve(request.fileType(), request.sourceObjectKey()).normalized();
            FileToPdfConverter converter = converterRegistry.getConverter(fileType);
            ConvertedFile convertedFile = converter.convert(inputStream, request.sourceObjectKey());
            String resultObjectKey = storagePathResolver.toPdfObjectKey(request.sourceObjectKey());

            minioService.upload(
                    sourceBucket,
                    resultObjectKey,
                    convertedFile.content(),
                    convertedFile.size(),
                    convertedFile.contentType()
            );

            if (conversionProperties.deleteSourceAfterConversion()) {
                minioService.delete(sourceBucket, request.sourceObjectKey());
            }

            return new ConversionExecutionResult(sourceBucket, resultObjectKey);
        } catch (Exception exception) {
            throw new FileStorageException("Failed to execute conversion for object '" + request.sourceObjectKey() + "'", exception);
        }
    }
}
