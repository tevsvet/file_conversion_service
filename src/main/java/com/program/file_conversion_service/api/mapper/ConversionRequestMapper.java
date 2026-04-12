package com.program.file_conversion_service.api.mapper;

import com.program.file_conversion_service.api.dto.CreateConversionRequest;
import com.program.file_conversion_service.kafka.dto.ConvertRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ConversionRequestMapper {

    @Mapping(target = "taskId", source = "taskId", qualifiedByName = "toTaskId")
    ConvertRequest toCommand(CreateConversionRequest request);

    @Named("toTaskId")
    default UUID toTaskId(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            return UUID.randomUUID();
        }
        return UUID.fromString(taskId);
    }
}
