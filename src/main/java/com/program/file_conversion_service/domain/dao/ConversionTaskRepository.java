package com.program.file_conversion_service.domain.dao;

import com.program.file_conversion_service.domain.model.ConversionTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConversionTaskRepository extends JpaRepository<ConversionTaskEntity, UUID> {
}
