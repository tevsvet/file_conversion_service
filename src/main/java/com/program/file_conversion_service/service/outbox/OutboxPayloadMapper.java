package com.program.file_conversion_service.service.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.program.file_conversion_service.kafka.dto.ConvertResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPayloadMapper {

    private final ObjectMapper objectMapper;

    public String serialize(ConvertResult payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Outbox payload must not be null");
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize outbox payload", exception);
        }
    }

    public ConvertResult deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, ConvertResult.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize outbox payload", exception);
        }
    }
}
