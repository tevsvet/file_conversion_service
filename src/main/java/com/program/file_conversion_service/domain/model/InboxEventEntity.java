package com.program.file_conversion_service.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inbox_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboxEventEntity {

    @Id
    private UUID id;

    @Column(name = "task_id", nullable = false, unique = true)
    private UUID taskId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InboxStatus status;

    @Column(name = "source_bucket", nullable = false)
    private String sourceBucket;

    @Column(name = "source_object_key", nullable = false)
    private String sourceObjectKey;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "available_at", nullable = false)
    private LocalDateTime availableAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (availableAt == null) {
            availableAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
