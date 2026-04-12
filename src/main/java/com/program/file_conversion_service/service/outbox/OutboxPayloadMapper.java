package com.program.file_conversion_service.service.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.program.file_conversion_service.domain.model.OutboxEventType;
import com.program.file_conversion_service.kafka.dto.ConvertRequest;
import com.program.file_conversion_service.kafka.dto.ConvertResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPayloadMapper {

    private final ObjectMapper objectMapper;

    public String serialize(OutboxEventType eventType, Object payload) {
        Class<?> expectedClass = payloadClass(eventType);
        if (!expectedClass.isInstance(payload)) {
            throw new IllegalArgumentException("Payload type %s does not match event type %s".formatted(
                    payload.getClass().getName(),
                    eventType
            ));
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize payload for event type " + eventType, exception);
        }
    }

    public Object deserialize(OutboxEventType eventType, String payload) {
        try {
            return objectMapper.readValue(payload, payloadClass(eventType));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize payload for event type " + eventType, exception);
        }
    }

    private Class<?> payloadClass(OutboxEventType eventType) {
        return switch (eventType) {
            case CONVERSION_REQUEST -> ConvertRequest.class;
            case CONVERSION_RESULT -> ConvertResult.class;
        };
    }
}
