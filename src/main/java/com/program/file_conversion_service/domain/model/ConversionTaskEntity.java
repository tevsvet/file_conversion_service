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
@Table(name = "conversion_task")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionTaskEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversionStatus status;

    @Column(name = "source_bucket", nullable = false)
    private String sourceBucket;

    @Column(name = "source_object_key", nullable = false)
    private String sourceObjectKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_file_type", nullable = false)
    private SupportedFileType sourceFileType;

    @Column(name = "result_bucket")
    private String resultBucket;

    @Column(name = "result_object_key")
    private String resultObjectKey;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
