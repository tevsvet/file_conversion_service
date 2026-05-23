package com.program.file_conversion_service.domain.dao;

import com.program.file_conversion_service.domain.model.InboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InboxEventRepository extends JpaRepository<InboxEventEntity, UUID> {

    @Query(value = """
            select * from inbox_event
            where status = :status and available_at <= now()
            order by created_at
            limit :batchSize
            for update skip locked
            """, nativeQuery = true)
    List<InboxEventEntity> findNextBatchForUpdate(@Param("status") String status,
                                                  @Param("batchSize") int batchSize);

    @Query(value = """
            select * from inbox_event
            where status = :status and updated_at <= :threshold
            order by created_at
            limit :batchSize
            for update skip locked
            """, nativeQuery = true)
    List<InboxEventEntity> findTimedOutProcessingBatchForUpdate(@Param("status") String status,
                                                                @Param("threshold")LocalDateTime threshold,
                                                                @Param("batchSize") int batchSize);
}
