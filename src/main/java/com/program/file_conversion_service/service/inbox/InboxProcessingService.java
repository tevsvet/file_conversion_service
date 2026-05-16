package com.program.file_conversion_service.service.inbox;

import com.program.file_conversion_service.domain.model.InboxEventEntity;
import com.program.file_conversion_service.kafka.dto.ConvertRequest;
import com.program.file_conversion_service.service.conversion.FileConversionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboxProcessingService {

    private final InboxService inboxService;
    private final FileConversionService fileConversionService;

    @Scheduled(fixedDelayString = "${app.inbox.poll-interval-ms}")
    public void processPendingInboxEvents() {
        inboxService.recoverStaleProcessingEvents();
        List<InboxEventEntity> batch = inboxService.claimNextBatch();
        for (InboxEventEntity inboxEvent : batch) {
            processSingle(inboxEvent);
        }
    }

    public void processSingle(InboxEventEntity inboxEvent) {
        try {
            ConvertRequest request = inboxService.deserializePayload(inboxEvent);
            fileConversionService.process(inboxEvent, request);
            inboxService.markProcessed(inboxEvent.getId());
        } catch (Exception exception) {
            log.error("Failed to process inbox event for taskId={}", inboxEvent.getTaskId(), exception);
            inboxService.registerFailure(inboxEvent.getId(), exception.getMessage());
        }
    }
}
