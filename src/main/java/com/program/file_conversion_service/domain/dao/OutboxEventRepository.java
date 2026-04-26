package com.program.file_conversion_service.domain.dao;

import com.program.file_conversion_service.domain.model.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    boolean existsByDedupKey(String dedupKey);

    @Query(value = """
            select * from outbox_event
            where status = :status and available_at <= now()
            order by created_at
            limit :batchSize
            for update skip locked
            """, nativeQuery = true)
    List<OutboxEventEntity> findNextBatchForUpdate(@Param("status") String status, @Param("batchSize") int batchSize);
}
