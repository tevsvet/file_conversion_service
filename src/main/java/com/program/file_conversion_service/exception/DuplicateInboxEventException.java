package com.program.file_conversion_service.exception;

import java.util.UUID;

public class DuplicateInboxEventException extends RuntimeException {

    public DuplicateInboxEventException(UUID taskId, Throwable cause) {
        super("Inbox event for taskId " + taskId + " has already been registered", cause);
    }
}

