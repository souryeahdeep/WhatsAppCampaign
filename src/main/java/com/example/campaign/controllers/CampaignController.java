package com.example.campaign.controllers;

import com.example.campaign.entity.Campaign;
import com.example.campaign.entity.ExportJob;
import com.example.campaign.enums.ExportStatus;
import com.example.campaign.repo.CampaignRepo;
import com.example.campaign.repo.ExportJobRepo;
import com.example.campaign.repo.MessageRepo;
import com.example.campaign.services.ExportService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.example.campaign.controllers.ExportController.getMapResponseEntity;

@CrossOrigin(value = {"http://localhost:5173", "http://localhost:5174"})
@RestController
@RequestMapping("/campaigns")
public class CampaignController {

    private final ExportJobRepo exportJobRepository;
    private final CampaignRepo campaignRepo;
    private final ExportService exportService;
    private final MessageRepo messageRepo;

    // fixed constructor: accept the needed beans once
    public CampaignController(CampaignRepo campaignRepo, ExportJobRepo exportJobRepository, ExportService exportService, MessageRepo messageRepo) {
        this.exportJobRepository = exportJobRepository;
        this.campaignRepo = campaignRepo;
        this.exportService = exportService;
        this.messageRepo = messageRepo;
    }

    @PostMapping("/{id}/export")
    public ExportJob createExport(@PathVariable Long id) {
        ExportJob job = new ExportJob();
        job.setId(UUID.randomUUID());
        job.setCampaignId(id);
        job.setStatus(ExportStatus.PENDING);
        exportJobRepository.save(job);

        exportService.runExport(job.getId());

        return job;
    }

    @GetMapping("/exports/{jobId}/download")
    public ResponseEntity<Resource> downloadCsv(@PathVariable UUID jobId) {
        ExportJob job = exportJobRepository.findById(jobId).orElseThrow();
        System.out.println(job.getStatus());
        if (job.getStatus() != ExportStatus.COMPLETED) {
            return ResponseEntity.badRequest().build();
        }

        Path filePath = Paths.get(job.getFilePath());
        Resource resource = new FileSystemResource(filePath);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"campaign-" + job.getCampaignId() + ".csv\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body( resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Campaign>> getMessages(@PathVariable Long id) {
        System.out.println(id);
        return ResponseEntity.ok().body(campaignRepo.findById(id));
    }

    // New endpoint: GET /exports/{jobId}/status
    // Responds with JSON: { "status": "COMPLETED" }
    @GetMapping("/exports/{jobId}/status")
    public ResponseEntity<Map<String, String>> getExportStatus(@PathVariable UUID jobId) {
        return getMapResponseEntity(jobId, exportJobRepository);
    }

}
