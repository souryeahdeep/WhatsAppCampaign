package com.example.campaign.entity;

import com.example.campaign.enums.ExportStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "export_jobs")
@Data
public class ExportJob {

    @Id
    private UUID id;

    private Long campaignId;
    private Long businessId;

    @Enumerated(EnumType.STRING)
    private ExportStatus status;

    private String filePath;     // local CSV path

    private Instant createdAt;
    private Instant completedAt;
    private String errorMessage;
}
