package com.program.file_conversion_service.service.task;

import com.program.file_conversion_service.domain.dao.ConversionTaskRepository;
import com.program.file_conversion_service.domain.model.ConversionStatus;
import com.program.file_conversion_service.domain.model.ConversionTaskEntity;
import com.program.file_conversion_service.domain.model.SupportedFileType;
import com.program.file_conversion_service.exception.ConversionTaskNotFoundException;
import com.program.file_conversion_service.kafka.dto.ConvertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversionTaskService {

    private final ConversionTaskRepository repository;

    @Transactional
    public ConversionTaskEntity registerPending(ConvertRequest request, String defaultBucket) {
        return repository.findById(request.taskId())
                .orElseGet(() -> repository.save(ConversionTaskEntity.builder()
                        .id(request.taskId())
                        .status(ConversionStatus.PENDING)
                        .sourceBucket(resolveBucket(request, defaultBucket))
                        .sourceObjectKey(request.sourceObjectKey())
                        .sourceFileType(SupportedFileType.resolve(request.fileType(), request.sourceObjectKey()).normalized())
                        .build()));
    }

    @Transactional(readOnly = true)
    public ConversionTaskEntity getById(UUID taskId) {
        return repository.findById(taskId)
                .orElseThrow(() -> new ConversionTaskNotFoundException("Conversion task not found: " + taskId));
    }

    @Transactional
    public boolean tryMarkInProgress(UUID taskId) {
        ConversionTaskEntity task = getById(taskId);
        if (task.getStatus() != ConversionStatus.PENDING) {
            return false;
        }
        task.setStatus(ConversionStatus.IN_PROGRESS);
        return true;
    }

    @Transactional
    public void markSuccess(UUID taskId, String resultBucket, String resultObjectKey) {
        ConversionTaskEntity task = getById(taskId);
        task.setStatus(ConversionStatus.SUCCESS);
        task.setResultBucket(resultBucket);
        task.setResultObjectKey(resultObjectKey);
        task.setErrorMessage(null);
    }

    @Transactional
    public void markFailed(UUID taskId, String errorMessage) {
        ConversionTaskEntity task = getById(taskId);
        task.setStatus(ConversionStatus.FAILED);
        task.setErrorMessage(errorMessage);
    }

    @Transactional(readOnly = true)
    public boolean isSubmittable(ConversionTaskEntity task) {
        return task.getStatus() == ConversionStatus.PENDING;
    }

    @Transactional(readOnly = true)
    public boolean isTerminal(UUID taskId) {
        ConversionStatus status = getById(taskId).getStatus();
        return status == ConversionStatus.SUCCESS || status == ConversionStatus.FAILED;
    }

    private String resolveBucket(ConvertRequest request, String defaultBucket) {
        return StringUtils.hasText(request.bucket()) ? request.bucket() : defaultBucket;
    }
}
