package com.example.campaign.entity;

import java.util.UUID;

public class ExportJobResponse {
    UUID jobId;
    String status;
    public ExportJobResponse(UUID jobId, String status) {
        this.jobId=jobId;
        this.status=status;
    }
}
