package com.program.file_conversion_service.service.storage;

import com.program.file_conversion_service.domain.model.ConversionTaskEntity;
import com.program.file_conversion_service.kafka.dto.ConvertRequest;
import com.program.file_conversion_service.minio.MinioService;
import com.program.file_conversion_service.service.request.ConversionRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final MinioService minioService;
    private final StoragePathResolver storagePathResolver;
    private final ConversionRequestService conversionRequestService;

    public ConversionTaskEntity upload(MultipartFile file, String bucket, String fileType, String taskId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file must not be empty");
        }

        UUID resolvedTaskId = taskId == null || taskId.isBlank() ? UUID.randomUUID() : UUID.fromString(taskId);
        String sourceObjectKey = storagePathResolver.toSourceObjectKey(file.getOriginalFilename(), resolvedTaskId);
        String resolvedBucket = minioService.resolveBucket(bucket);
        String contentType = file.getContentType() == null || file.getContentType().isBlank()
                ? "application/octet-stream"
                : file.getContentType();

        minioService.upload(
                resolvedBucket,
                sourceObjectKey,
                file.getBytes(),
                file.getSize(),
                contentType
        );

        try {
            return conversionRequestService.submit(new ConvertRequest(
                    resolvedTaskId,
                    resolvedBucket,
                    sourceObjectKey,
                    fileType
            ));
        } catch (RuntimeException exception) {
            minioService.delete(resolvedBucket, sourceObjectKey);
            throw exception;
        }
    }
}
