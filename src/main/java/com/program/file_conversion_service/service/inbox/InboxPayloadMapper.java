package com.program.file_conversion_service.service.inbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.program.file_conversion_service.kafka.dto.ConvertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InboxPayloadMapper {

    private final ObjectMapper objectMapper;

    public String serialize(ConvertRequest payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Inbox payload must not be null");
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize inbox payload", exception);
        }
    }

    public ConvertRequest deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, ConvertRequest.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize inbox payload", exception);
        }
    }
}
