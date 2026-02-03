package com.example.campaign.services;

import com.example.campaign.entity.ExportJob;
import com.example.campaign.entity.ExportJobResponse;
import com.example.campaign.enums.ExportStatus;
import com.example.campaign.repo.ExportJobRepo;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExportJobService {
    private final ExportJobRepo exportJobRepo;
    public ExportJobService(ExportJobRepo exportJobRepo) {
        this.exportJobRepo = exportJobRepo;
    }
    public ExportJobResponse createExportJob(Long campaignId) {
        ExportJob job = new ExportJob();
        job.setId(UUID.randomUUID());
        job.setCampaignId(campaignId);
        job.setStatus(ExportStatus.PENDING);
        exportJobRepo.save(job);


        return new ExportJobResponse(job.getId(), "PENDING");
    }
}
