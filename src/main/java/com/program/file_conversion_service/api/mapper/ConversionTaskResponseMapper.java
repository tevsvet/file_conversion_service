package com.program.file_conversion_service.api.mapper;

import com.program.file_conversion_service.api.dto.ConversionTaskResponse;
import com.program.file_conversion_service.domain.model.ConversionTaskEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConversionTaskResponseMapper {

    ConversionTaskResponse toResponse(ConversionTaskEntity task);
}
